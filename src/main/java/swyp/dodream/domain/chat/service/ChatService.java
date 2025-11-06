package swyp.dodream.domain.chat.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.domain.chat.domain.*;
import swyp.dodream.domain.chat.dto.ChatInitiateResponse;
import swyp.dodream.domain.chat.dto.ChatMessageDto;
import swyp.dodream.domain.chat.repository.*;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * CHAT-01, BR-02-02: '채팅하기' 버튼 클릭 시 (REST API)
     *
     * @param postId   게시글 ID
     * @param memberId 채팅을 시작하려는 유저(멤버) ID
     * @return ChatInitiateResponse (roomId, topicId, history 등)
     */
    @Transactional(readOnly = true)
    public ChatInitiateResponse initiateChat(Long postId, Long memberId) {
        Post post = postRepository.findById(postId) // soft delete(@Where) 적용됨
                .orElseThrow(() -> new EntityNotFoundException("모집글을 찾을 수 없습니다. ID: " + postId));

        // post.owner.id가 리더 ID
        Long leaderId = post.getOwner().getId();

        // 정책: 리더는 (이 버튼으로) 채팅방 생성 불가
        if (memberId.equals(leaderId)) {
            throw new AccessDeniedException("리더는 '채팅하기' 버튼을 사용할 수 없습니다.");
        }

        // 정책: 리더 계정이 활성 상태인지 확인
        // (주의) statusTrue를 확인하는 메서드 사용
        if (!userRepository.existsByIdAndStatusTrue(leaderId)) {
            throw new EntityNotFoundException("리더의 계정이 존재하지 않거나 비활성화되었습니다.");
        }

        // 예측 가능한 토픽 ID 생성
        String topicId = buildTopicId(postId, leaderId, memberId);

        // 1. 기존 1:1 채팅방이 있는지 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByPostIdAndLeaderUserIdAndMemberUserId(postId, leaderId, memberId);

        if (existingRoom.isPresent()) {
            // 2. 채팅방이 있으면
            ChatRoom room = existingRoom.get();
            // 2-1. 참여자가 나간 상태인지 확인 (재입장 불가 정책)
            checkParticipantStatus(room.getId(), memberId);

            // 2-2. 기존 메시지 내역 로드
            List<ChatMessageDto> history = chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(room.getId())
                    .stream()
                    .map(ChatMessage::toDto)
                    .collect(Collectors.toList());

            log.info("기존 채팅방 입장. RoomId: {}, TopicId: {}", room.getId(), topicId);
            return new ChatInitiateResponse(room.getId(), topicId, leaderId, memberId, history);
        } else {
            // 3. 채팅방이 없으면 (DB 생성 X)
            // 첫 메시지 전송 시 생성되도록 null roomId와 토픽 정보 반환
            log.info("신규 채팅방 초기화. TopicId: {}", topicId);
            return new ChatInitiateResponse(null, topicId, leaderId, memberId, Collections.emptyList());
        }
    }

    /**
     * WebSocket을 통해 메시지 수신 시 처리 (핵심 로직)
     *
     * @param messageDto 수신된 메시지 DTO
     * @param senderId   보낸 사람 ID (SecurityContext에서 추출)
     * @return 저장된 ChatMessage 엔티티
     */
    @Transactional
    public ChatMessage processMessage(ChatMessageDto messageDto, Long senderId) {
        messageDto.setSenderId(senderId);
        String topicId;
        ChatRoom room;

        if (messageDto.getRoomId() == null) {
            // 1. 첫 메시지인 경우 (roomId가 null)
            Post post = postRepository.findById(messageDto.getPostId())
                    .orElseThrow(() -> new EntityNotFoundException("모집글을 찾을 수 없습니다. ID: " + messageDto.getPostId()));

            // (주의) 리더 ID
            Long leaderId = post.getOwner().getId();
            Long memberId;

            // 정책: 리더가 아닌 유저만 채팅방 생성 가능 (즉, sender가 member)
            // (시나리오 [리더]의 경우, initiateChat에서 이미 생성된 방을 반환받으므로 이 로직을 타지 않음)
            if (senderId.equals(leaderId)) {
                // 리더가 첫 메시지를 보내는 경우 (e.g. 멤버가 initiate만 하고 메시지 안 보냄)
                if (messageDto.getReceiverId() == null ||
                        !userRepository.existsByIdAndStatusTrue(messageDto.getReceiverId()) || // (주의)
                        messageDto.getReceiverId().equals(leaderId)) {
                    throw new IllegalArgumentException("잘못된 수신자입니다.");
                }
                memberId = messageDto.getReceiverId();
            } else {
                // 멤버가 첫 메시지를 보내는 경우
                if (messageDto.getReceiverId() == null || !leaderId.equals(messageDto.getReceiverId())) {
                    throw new IllegalArgumentException("수신자가 모집글의 리더와 일치하지 않습니다.");
                }
                memberId = senderId;
            }

            // 1-2. 채팅방 생성 또는 조회 (Race Condition 처리 포함)
            room = findOrCreateRoom(post.getId(), leaderId, memberId);
            topicId = buildTopicId(post.getId(), leaderId, memberId);
            messageDto.setRoomId(room.getId());

        } else {
            // 2. 기존 채팅방의 후속 메시지인 경우
            room = chatRoomRepository.findById(messageDto.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다. ID: " + messageDto.getRoomId()));

            // 2-1. 참여자가 나간 상태인지 확인 (재입장 불가)
            checkParticipantStatus(room.getId(), senderId);

            topicId = buildTopicId(room.getPostId(), room.getLeaderUserId(), room.getMemberUserId());
        }

        // 3. 메시지 저장
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(room.getId());
        chatMessage.setSenderUserId(senderId);
        chatMessage.setBody(messageDto.getBody());

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.debug("메시지 저장 완료. RoomId: {}, MsgId: {}", savedMessage.getRoomId(), savedMessage.getId());

        // 4. WebSocket 토픽으로 메시지 브로드캐스트
        messagingTemplate.convertAndSend(topicId, savedMessage.toDto());
        log.debug("메시지 브로드캐스트. Topic: {}", topicId);

        return savedMessage;
    }

    /**
     * [findOrCreateRoom]
     * 첫 메시지 전송 시점에 채팅방과 참여자를 생성합니다.
     */
    @Transactional
    private ChatRoom findOrCreateRoom(Long postId, Long leaderId, Long memberId) {
        try {
            // 1. 기존 채팅방 조회 (동시성 제어를 위해 DB 락을 고려할 수 있으나, Unique 제약조건으로 1차 방어)
            Optional<ChatRoom> existingRoom = chatRoomRepository.findByPostIdAndLeaderUserIdAndMemberUserId(postId, leaderId, memberId);
            if (existingRoom.isPresent()) {
                ChatRoom room = existingRoom.get();
                if (room.getFirstMessageAt() == null) {
                    room.setFirstMessageAt(LocalDateTime.now());
                    return chatRoomRepository.save(room);
                }
                return room;
            }

            // 2. 신규 채팅방 생성
            ChatRoom newRoom = new ChatRoom();
            newRoom.setPostId(postId);
            newRoom.setLeaderUserId(leaderId);
            newRoom.setMemberUserId(memberId);
            newRoom.setFirstMessageAt(LocalDateTime.now()); // (정책) 첫 메시지 전송 시각 기록

            ChatRoom savedRoom = chatRoomRepository.save(newRoom);

            // 3. 참여자 2명 생성 (리더, 멤버)
            ChatParticipant leaderPart = new ChatParticipant(savedRoom.getId(), leaderId);
            ChatParticipant memberPart = new ChatParticipant(savedRoom.getId(), memberId);
            chatParticipantRepository.saveAll(Arrays.asList(leaderPart, memberPart));

            // 4. (정책) 첫 메시지 전송 시 알림 발송 -> (요청에 따라 제거)
            // notificationService.send(leaderId, memberId, postId, "새로운 채팅이 시작되었습니다.");

            log.info("신규 채팅방 생성 완료. RoomId: {}", savedRoom.getId());
            return savedRoom;

        } catch (DataIntegrityViolationException e) {
            // (동시성 제어) 유니크 키 제약조건 위반 시 (거의 동시에 두 유저가 생성 시도)
            log.warn("채팅방 생성 중 Race Condition 발생. 기존 방 조회. PostId: {}, Leader: {}, Member: {}", postId, leaderId, memberId);
            return chatRoomRepository.findByPostIdAndLeaderUserIdAndMemberUserId(postId, leaderId, memberId)
                    .orElseThrow(() -> new IllegalStateException("채팅방 생성/조회 실패 (Race Condition 후)"));
        }
    }

    /**
     * CHAT-02: 채팅방 나가기
     */
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ChatParticipant participant = chatParticipantRepository.findById(new ChatParticipantId(roomId, userId))
                .orElseThrow(() -> new EntityNotFoundException("채팅방 참여자가 아닙니다."));

        if (participant.getLeftAt() == null) {
            participant.setLeftAt(LocalDateTime.now());
            chatParticipantRepository.save(participant);
            log.info("유저가 채팅방을 나갔습니다. RoomId: {}, UserId: {}", roomId, userId);

            // 상대방에게 "상대방이 나갔습니다" 시스템 메시지 전송
            ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
            String topicId = buildTopicId(room.getPostId(), room.getLeaderUserId(), room.getMemberUserId());

            ChatMessageDto leaveMessage = new ChatMessageDto(
                    null, roomId, room.getPostId(), userId, null,
                    "상대방이 채팅방을 나갔습니다.", // TODO: (선택) 유저 이름으로 변경
                    LocalDateTime.now(), ChatMessageDto.MessageType.LEAVE
            );
            messagingTemplate.convertAndSend(topicId, leaveMessage);
        }
    }

    // --- Helper Methods ---

    /**
     * 구독할 토픽 ID를 생성합니다. (e.g., /topic/chat/post/1/leader/10/member/20)
     */
    private String buildTopicId(Long postId, Long leaderId, Long memberId) {
        // leaderId와 memberId는 고정된 값이므로 정렬할 필요 없음 (ERD 기반)
        return String.format("/topic/chat/post/%d/leader/%d/member/%d", postId, leaderId, memberId);
    }

    /**
     * 유저가 채팅방을 나갔는지 확인 (재입장 불가 정책)
     */
    private void checkParticipantStatus(Long roomId, Long userId) {
        chatParticipantRepository.findById(new ChatParticipantId(roomId, userId))
                .ifPresent(p -> {
                    if (p.getLeftAt() != null) {
                        throw new AccessDeniedException("이미 나간 채팅방입니다. 재입장할 수 없습니다.");
                    }
                });
    }
}