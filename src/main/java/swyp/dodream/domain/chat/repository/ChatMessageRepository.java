package swyp.dodream.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.chat.domain.ChatMessage;
import swyp.dodream.domain.chat.domain.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 채팅방의 모든 메시지를 시간순으로 조회합니다. (성능 문제 -> 주석 처리)
//    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    // 최신 메시지 n개만 조회 (초기 로드용)
    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom = :chatRoom " +
            "ORDER BY cm.createdAt DESC " +
            "LIMIT :limit")
    List<ChatMessage> findRecentMessagesByRoom(
            @Param("chatRoom") ChatRoom chatRoom,
            @Param("limit") int limit
    );

    // 특정 메시지 ID 이전의 메시지 조회 (무한 스크롤용)
    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom = :chatRoom " +
            "AND cm.id < :lastMessageId " +
            "ORDER BY cm.createdAt DESC " +
            "LIMIT :limit")
    List<ChatMessage> findMessagesBeforeId(
            @Param("chatRoom") ChatRoom chatRoom,
            @Param("lastMessageId") String lastMessageId,
            @Param("limit") int limit
    );

    // 특정 채팅방의 마지막 메시지를 조회합니다.
    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom.id = :roomId " +
            "ORDER BY cm.createdAt DESC " +
            "LIMIT 1")
    Optional<ChatMessage> findLastMessageByRoomId(@Param("roomId") String roomId);

}