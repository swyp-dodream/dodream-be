package swyp.dodream.domain.chat.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.chat.domain.*;
import swyp.dodream.domain.chat.dto.response.ChatInitiateResponse;
import swyp.dodream.domain.chat.dto.ChatMessageDto;
import swyp.dodream.domain.chat.dto.response.MyChatListResponse;
import swyp.dodream.domain.chat.repository.*;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;

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
    private final SnowflakeIdService snowflakeIdService;

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    public static final String CHAT_TOPIC_PATTERN = "/topic/chat/post/{postId}/leader/{leaderId}/member/{memberId}";

    // ==================== 1. 채팅 시작 ====================
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

            String myRole = "MEMBER";

            log.info("기존 채팅방 입장. RoomId: {}, TopicId: {}", room.getId(), topicId);
            return new ChatInitiateResponse(
                    room.getId(),
                    topicId,
                    String.valueOf(leaderId),
                    String.valueOf(memberId),
                    myRole,
                    history
            );
        } else {
            log.info("신규 채팅방 초기화. TopicId: {}", topicId);
            return new ChatInitiateResponse(
                    null,
                    topicId,
                    String.valueOf(leaderId),
                    String.valueOf(memberId),
                    "MEMBER",
                    Collections.emptyList()
            );
        }
    }

    // ==================== 2. 메시지 처리 ====================
    @Transactional
    public ChatMessage processMessage(ChatMessageDto messageDto, Long senderId) {
        if (senderId == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        messageDto.setSenderId(String.valueOf(senderId));

        String topicId;
        ChatRoom room;

        if (messageDto.getRoomId() == null) {
            Long postIdLong = Long.parseLong(messageDto.getPostId());
            Post post = postRepository.findById(postIdLong)
                    .orElseThrow(() -> new EntityNotFoundException("모집글을 찾을 수 없습니다."));

            Long leaderId = post.getOwner().getId();
            Long memberId;

            if (senderId.equals(leaderId)) {
                Long receiverIdLong = messageDto.getReceiverId() != null
                        ? Long.parseLong(messageDto.getReceiverId()) : null;
                if (receiverIdLong == null || receiverIdLong.equals(leaderId)) {
                    throw new IllegalArgumentException("잘못된 수신자입니다.");
                }
                memberId = receiverIdLong;
            } else {
                Long receiverIdLong = messageDto.getReceiverId() != null
                        ? Long.parseLong(messageDto.getReceiverId()) : null;
                if (receiverIdLong == null || !leaderId.equals(receiverIdLong)) {
                    throw new IllegalArgumentException("수신자가 모집글의 리더와 일치하지 않습니다.");
                }
                memberId = senderId;
            }

            room = findOrCreateRoom(postIdLong, leaderId, memberId);
            topicId = buildTopicId(postIdLong, leaderId, memberId);
            messageDto.setRoomId(room.getId());

        } else {
            room = chatRoomRepository.findById(messageDto.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));
            checkParticipantStatus(room.getId(), senderId);
            topicId = buildTopicId(room.getPostId(), room.getLeaderUserId(), room.getMemberUserId());
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(snowflakeIdService.nextStringId());  // 직접 할당
        chatMessage.setChatRoom(room);
        chatMessage.setSenderUserId(senderId);
        chatMessage.setBody(messageDto.getBody());
        chatMessage.setDeletedAt(false);

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

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
        return savedMessage;
    }

    // ==================== 3. 구독 검증 ====================
    public void validateSubscription(Long userId, String topicDestination) {
        if (!pathMatcher.match(CHAT_TOPIC_PATTERN, topicDestination)) {
            throw new AccessDeniedException("유효하지 않은 채팅방 토픽입니다.");
        }

        Map<String, String> uriVariables = pathMatcher.extractUriTemplateVariables(CHAT_TOPIC_PATTERN, topicDestination);
        Long leaderId = Long.parseLong(uriVariables.get("leaderId"));
        Long memberId = Long.parseLong(uriVariables.get("memberId"));

        if (!userId.equals(leaderId) && !userId.equals(memberId)) {
            throw new AccessDeniedException("이 채팅방을 구독할 권한이 없습니다.");
        }
    }

    // ==================== 4. 채팅방 생성/조회 ====================
    private ChatRoom findOrCreateRoom(Long postId, Long leaderId, Long memberId) {
        Optional<ChatRoom> existingRoom = chatRoomRepository
                .findByPostIdAndLeaderUserIdAndMemberUserId(postId, leaderId, memberId);

        if (existingRoom.isPresent()) {
            ChatRoom room = existingRoom.get();
            if (room.getFirstMessageAt() == null) {
                room.setFirstMessageAt(LocalDateTime.now());
                return chatRoomRepository.save(room);
            }
            return room;
        }

        ChatRoom newRoom = new ChatRoom(postId, leaderId, memberId, snowflakeIdService);
        ChatRoom savedRoom = chatRoomRepository.save(newRoom);

        ChatParticipant leaderPart = new ChatParticipant(savedRoom, leaderId);
        ChatParticipant memberPart = new ChatParticipant(savedRoom, memberId);
        chatParticipantRepository.saveAll(Arrays.asList(leaderPart, memberPart));

        log.info("신규 채팅방 생성 완료. RoomId: {}", savedRoom.getId());
        return savedRoom;
    }

    // ==================== 5. 나가기 ====================
    @Transactional
    public void leaveRoom(String roomId, Long userId) {
        ChatParticipant participant = chatParticipantRepository.findById(new ChatParticipantId(roomId, userId))
                .orElseThrow(() -> new EntityNotFoundException("채팅방 참여자가 아닙니다."));

        if (participant.getLeftAt() == null) {
            participant.leave();  // 메서드 사용
            chatParticipantRepository.save(participant);

            ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
            String topicId = buildTopicId(room.getPostId(), room.getLeaderUserId(), room.getMemberUserId());

            ChatMessageDto leaveMessage = new ChatMessageDto(
                    null, roomId, String.valueOf(room.getPostId()),
                    String.valueOf(userId), null,
                    "상대방이 채팅방을 나갔습니다.",
                    LocalDateTime.now(),
                    ChatMessageDto.MessageType.LEAVE
            );
            messagingTemplate.convertAndSend(topicId, leaveMessage);
        }
    }

    // ==================== 6. 내 채팅방 목록 ====================
    @Transactional(readOnly = true)
    public List<MyChatListResponse> getMyChatRooms(Long myUserId, ChatFilterType filter) {
        return chatParticipantRepository.findAllByUserId(myUserId).stream()
                .filter(cp -> cp.getLeftAt() == null)  // 나간 채팅방 제외
                .map(cp -> {
                    ChatRoom room = cp.getChatRoom();
                    Long otherUserId = myUserId.equals(room.getLeaderUserId())
                            ? room.getMemberUserId() : room.getLeaderUserId();
                    String roomName = userRepository.findById(otherUserId)
                            .map(User::getName)
                            .orElse("알 수 없는 사용자");

                    Long unReadCount = readStatusRepository.countByChatRoomAndUserAndIsReadFalse(room,
                            userRepository.getReferenceById(myUserId));

                    String myRole = myUserId.equals(room.getLeaderUserId()) ? "LEADER" : "MEMBER";
                    String topicId = buildTopicId(room.getPostId(), room.getLeaderUserId(), room.getMemberUserId());

                    return MyChatListResponse.builder()
                            .roomId(room.getId())
                            .roomName(roomName)
                            .unReadCount(unReadCount)
                            .topicId(topicId)
                            .leaderId(String.valueOf(room.getLeaderUserId()))
                            .memberId(String.valueOf(room.getMemberUserId()))
                            .myRole(myRole)
                            .build();
                })
                .filter(response -> {
                    if (filter == ChatFilterType.UNREAD) {
                        return response.getUnReadCount() > 0;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

        // ==================== 7. 읽음 처리 ====================
    @Transactional
    public int messageRead(String roomId, Long myUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));
        User me = userRepository.findById(myUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        checkParticipantStatus(roomId, myUserId);

        List<ReadStatus> unreadStatuses = readStatusRepository.findByChatRoomAndUserAndIsReadFalse(chatRoom, me);
        unreadStatuses.forEach(r -> r.updateIsRead(true));

        int count = unreadStatuses.size();
        log.info("메시지 읽음 처리. RoomId: {}, UserId: {}, Count: {}", roomId, myUserId, count);

        return count;
    }


    // ==================== 8. 채팅 내역 조회 ====================
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(String roomId, Long myUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        boolean isLeader = myUserId.equals(chatRoom.getLeaderUserId());

        if (!isLeader) {
            ChatParticipant participant = chatParticipantRepository.findById(
                            new ChatParticipantId(roomId, myUserId))
                    .orElseThrow(() -> new IllegalArgumentException("본인이 속하지 않은 채팅방입니다."));

            if (participant.getLeftAt() != null) {
                throw new AccessDeniedException("이미 나간 채팅방의 대화 내역은 조회할 수 없습니다.");
            }
        }

        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom)
                .stream()
                .map(ChatMessage::toDto)
                .collect(Collectors.toList());
    }

    // ==================== Helper ====================
    private String buildTopicId(Long postId, Long leaderId, Long memberId) {
        return String.format("/topic/chat/post/%d/leader/%d/member/%d", postId, leaderId, memberId);
    }

    private void checkParticipantStatus(String roomId, Long userId) {
        chatParticipantRepository.findById(new ChatParticipantId(roomId, userId))
                .ifPresent(p -> {
                    if (p.getLeftAt() != null) {
                        throw new AccessDeniedException("이미 나간 채팅방입니다. 재입장할 수 없습니다.");
                    }
                });
    }
}