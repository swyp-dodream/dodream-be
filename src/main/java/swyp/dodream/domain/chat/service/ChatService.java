package swyp.dodream.domain.chat.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
// import org.springframework.security.core.context.SecurityContextHolder; // [수정] 사용하지 않는 import 제거
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import swyp.dodream.domain.chat.domain.*;
import swyp.dodream.domain.chat.dto.response.ChatInitiateResponse;
import swyp.dodream.domain.chat.dto.ChatMessageDto;
import swyp.dodream.domain.chat.dto.response.MyChatListResponse;
import swyp.dodream.domain.chat.repository.*;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;
import swyp.dodream.domain.chat.domain.ReadStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    public static final String CHAT_TOPIC_PATTERN = "/topic/chat/post/{postId}/leader/{leaderId}/member/{memberId}";

    @Transactional(readOnly = true)
    public ChatInitiateResponse initiateChat(Long postId, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("모집글을 찾을 수 없습니다. ID: " + postId));

        Long leaderId = post.getOwner().getId();

        if (memberId.equals(leaderId)) {
            throw new AccessDeniedException("리더는 '채팅하기' 버튼을 사용할 수 없습니다.");
        }
        if (!userRepository.existsByIdAndStatusTrue(leaderId)) {
            throw new EntityNotFoundException("리더의 계정이 존재하지 않거나 비활성화되었습니다.");
        }

        String topicId = buildTopicId(postId, leaderId, memberId);

        Optional<ChatRoom> existingRoom = chatRoomRepository.findByPostIdAndLeaderUserIdAndMemberUserId(postId, leaderId, memberId);

        if (existingRoom.isPresent()) {
            ChatRoom room = existingRoom.get();
            checkParticipantStatus(room.getId(), memberId);

            List<ChatMessageDto> history = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(room)
                    .stream()
                    .map(ChatMessage::toDto)
                    .collect(Collectors.toList());

            log.info("기존 채팅방 입장. RoomId: {}, TopicId: {}", room.getId(), topicId);
            return new ChatInitiateResponse(room.getId(), topicId, leaderId, memberId, history);
        } else {
            log.info("신규 채팅방 초기화. TopicId: {}", topicId);
            return new ChatInitiateResponse(null, topicId, leaderId, memberId, Collections.emptyList());
        }
    }

    @Transactional
    public ChatMessage processMessage(ChatMessageDto messageDto, Long senderId) {
        messageDto.setSenderId(senderId);
        String topicId;
        ChatRoom room;

        if (messageDto.getRoomId() == null) {
            Post post = postRepository.findById(messageDto.getPostId())
                    .orElseThrow(() -> new EntityNotFoundException("모집글을 찾을 수 없습니다. ID: " + messageDto.getPostId()));

            Long leaderId = post.getOwner().getId();
            Long memberId;

            if (senderId.equals(leaderId)) {
                if (messageDto.getReceiverId() == null ||
                        !userRepository.existsByIdAndStatusTrue(messageDto.getReceiverId()) ||
                        messageDto.getReceiverId().equals(leaderId)) {
                    throw new IllegalArgumentException("잘못된 수신자입니다.");
                }
                memberId = messageDto.getReceiverId();
            } else {
                if (messageDto.getReceiverId() == null || !leaderId.equals(messageDto.getReceiverId())) {
                    throw new IllegalArgumentException("수신자가 모집글의 리더와 일치하지 않습니다.");
                }
                memberId = senderId;
            }

            room = findOrCreateRoom(post.getId(), leaderId, memberId);
            topicId = buildTopicId(post.getId(), leaderId, memberId);
            messageDto.setRoomId(room.getId());

        } else {
            room = chatRoomRepository.findById(messageDto.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다. ID: " + messageDto.getRoomId()));

            checkParticipantStatus(room.getId(), senderId);
            topicId = buildTopicId(room.getPostId(), room.getLeaderUserId(), room.getMemberUserId());
        }

        // 3. 메시지 저장
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatRoom(room);
        chatMessage.setSenderUserId(senderId);
        chatMessage.setBody(messageDto.getBody());
        chatMessage.setDeletedAt(false);

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.debug("메시지 저장 완료. RoomId: {}, MsgId: {}", savedMessage.getChatRoom().getId(), savedMessage.getId());

        Long recipientId = senderId.equals(room.getLeaderUserId()) ? room.getMemberUserId() : room.getLeaderUserId();
        userRepository.findById(recipientId).ifPresent(recipient -> {
            ReadStatus newStatus = ReadStatus.builder()
                    .chatRoom(room)
                    .user(recipient)
                    .chatMessage(savedMessage)
                    .isRead(false)
                    .build();
            readStatusRepository.save(newStatus);
        });

        messagingTemplate.convertAndSend(topicId, savedMessage.toDto());
        log.debug("메시지 브로드캐스트. Topic: {}", topicId);

        return savedMessage;
    }

    public void validateSubscription(Long userId, String topicDestination) {
        if (!pathMatcher.match(CHAT_TOPIC_PATTERN, topicDestination)) {
            log.warn("유효하지 않은 토픽 구독 시도: {}", topicDestination);
            throw new AccessDeniedException("유효하지 않은 채팅방 토픽입니다.");
        }

        java.util.Map<String, String> uriVariables =
                pathMatcher.extractUriTemplateVariables(CHAT_TOPIC_PATTERN, topicDestination);

        Long leaderId = Long.parseLong(uriVariables.get("leaderId"));
        Long memberId = Long.parseLong(uriVariables.get("memberId"));

        if (!userId.equals(leaderId) && !userId.equals(memberId)) {
            log.warn("채팅방 구독 권한 없음. UserId: {}, Topic: {}", userId, topicDestination);
            throw new AccessDeniedException("이 채팅방을 구독할 권한이 없습니다.");
        }
        log.debug("STOMP user subscribed: User: {}, Topic: {}", userId, topicDestination);
    }

    private ChatRoom findOrCreateRoom(Long postId, Long leaderId, Long memberId) {
        try {
            Optional<ChatRoom> existingRoom = chatRoomRepository.findByPostIdAndLeaderUserIdAndMemberUserId(postId, leaderId, memberId);
            if (existingRoom.isPresent()) {
                ChatRoom room = existingRoom.get();
                if (room.getFirstMessageAt() == null) {
                    room.setFirstMessageAt(LocalDateTime.now());
                    return chatRoomRepository.save(room);
                }
                return room;
            }

            ChatRoom newRoom = new ChatRoom();
            newRoom.setPostId(postId);
            newRoom.setLeaderUserId(leaderId);
            newRoom.setMemberUserId(memberId);
            newRoom.setFirstMessageAt(LocalDateTime.now());

            ChatRoom savedRoom = chatRoomRepository.save(newRoom);

            ChatParticipant leaderPart = new ChatParticipant(savedRoom, leaderId);
            ChatParticipant memberPart = new ChatParticipant(savedRoom, memberId);
            chatParticipantRepository.saveAll(Arrays.asList(leaderPart, memberPart));

            log.info("신규 채팅방 생성 완료. RoomId: {}", savedRoom.getId());
            return savedRoom;

        } catch (DataIntegrityViolationException e) {
            log.warn("채팅방 생성 중 Race Condition 발생. 기존 방 조회. PostId: {}, Leader: {}, Member: {}", postId, leaderId, memberId);
            return chatRoomRepository.findByPostIdAndLeaderUserIdAndMemberUserId(postId, leaderId, memberId)
                    .orElseThrow(() -> new IllegalStateException("채팅방 생성/조회 실패 (Race Condition 후)"));
        }
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ChatParticipant participant = chatParticipantRepository.findById(new ChatParticipantId(roomId, userId))
                .orElseThrow(() -> new EntityNotFoundException("채팅방 참여자가 아닙니다."));

        if (participant.getLeftAt() == null) {
            participant.setLeftAt(LocalDateTime.now());
            chatParticipantRepository.save(participant);
            log.info("유저가 채팅방을 나갔습니다. RoomId: {}, UserId: {}", roomId, userId);

            ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
            String topicId = buildTopicId(room.getPostId(), room.getLeaderUserId(), room.getMemberUserId());

            ChatMessageDto leaveMessage = new ChatMessageDto(
                    null, roomId, room.getPostId(), userId, null,
                    "상대방이 채팅방을 나갔습니다.",
                    LocalDateTime.now(), ChatMessageDto.MessageType.LEAVE
            );
            messagingTemplate.convertAndSend(topicId, leaveMessage);
        }
    }

    // 내 채팅방 조회
    @Transactional(readOnly = true)
    public List<MyChatListResponse> getMyChatRooms(Long myUserId) {
        // 1. 현재 유저 객체 조회
        User me = userRepository.findById(myUserId)
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 2. 내 참여 정보 조회
        List<ChatParticipant> myParticipants = chatParticipantRepository.findAllByUserId(myUserId);

        // 3. DTO로 변환
        return myParticipants.stream()
                .map(cp -> {
                    ChatRoom room = cp.getChatRoom();

                    // 3-1. 상대방 ID 및 이름 찾기
                    Long otherUserId = myUserId.equals(room.getLeaderUserId()) ? room.getMemberUserId() : room.getLeaderUserId();
                    String roomName = userRepository.findById(otherUserId)
                            .map(User::getName) // (User 엔티티에 getName()이 있다고 가정)
                            .orElse("알 수 없는 사용자");

                    // 3-2. 안 읽은 메시지 수 카운트 (새로운 ReadStatus 구조 기반)
                    Long unReadCount = readStatusRepository.countByChatRoomAndUserAndIsReadFalse(room, me);

                    return MyChatListResponse.builder()
                            .roomId(room.getId())
                            .roomName(roomName)
                            .unReadCount(unReadCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 메시지 읽음 처리
    @Transactional
    public void messageRead(Long roomId, Long myUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));
        User me = userRepository.findById(myUserId)
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 1. 참여자인지 확인 (checkParticipantStatus가 이미 AccessDeniedException 처리)
        checkParticipantStatus(roomId, myUserId);

        // 2. 이 유저가 해당 방에서 안 읽은 ReadStatus 조회 (최적화)
        List<ReadStatus> unreadStatuses = readStatusRepository.findByChatRoomAndUserAndIsReadFalse(chatRoom, me);

        // 3. 모두 읽음 처리
        for (ReadStatus r : unreadStatuses) {
            r.updateIsRead(true);
        }
        // @Transactional에 의해 dirty checking으로 자동 save (flush) 됨
        log.info("메시지 읽음 처리. RoomId: {}, UserId: {}, Count: {}", roomId, myUserId, unreadStatuses.size());
    }

    // 채팅 내역 조회
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(Long roomId, Long myUserId) {
        // 1. 내가 해당 채팅방의 참여자인지 확인 (나갔는지 여부 포함)
        ChatParticipant participant = chatParticipantRepository.findById(new ChatParticipantId(roomId, myUserId))
                .orElseThrow(() -> new IllegalArgumentException("본인이 속하지 않은 채팅방입니다."));

        // 2. CHAT-02 정책: 나간 채팅방 히스토리 조회 불가
        if (participant.getLeftAt() != null) {
            throw new AccessDeniedException("이미 나간 채팅방의 대화 내역은 조회할 수 없습니다.");
        }

        ChatRoom chatRoom = participant.getChatRoom();

        // 3. 특정 room에 대한 message조회
        // (findByRoomIdOrderByCreatedAtAsc -> findByChatRoomOrderByCreatedAtAsc)
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);

        // 4. DTO로 변환
        return chatMessages.stream()
                .map(ChatMessage::toDto) // (지난번에 추가한 toDto() 메서드 재활용)
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---
    private String buildTopicId(Long postId, Long leaderId, Long memberId) {
        return String.format("/topic/chat/post/%d/leader/%d/member/%d", postId, leaderId, memberId);
    }

    private void checkParticipantStatus(Long roomId, Long userId) {
        chatParticipantRepository.findById(new ChatParticipantId(roomId, userId))
                .ifPresent(p -> {
                    if (p.getLeftAt() != null) {
                        throw new AccessDeniedException("이미 나간 채팅방입니다. 재입장할 수 없습니다.");
                    }
                });
    }
}