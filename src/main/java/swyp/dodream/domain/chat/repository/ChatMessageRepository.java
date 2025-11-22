package swyp.dodream.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.chat.domain.ChatMessage;
import swyp.dodream.domain.chat.domain.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 특정 채팅방의 모든 메시지를 시간순으로 조회합니다.
     */
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    /**
     * 특정 채팅방의 마지막 메시지를 조회합니다.
     */
    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom.id = :roomId " +
            "ORDER BY cm.createdAt DESC " +
            "LIMIT 1")
    Optional<ChatMessage> findLastMessageByRoomId(@Param("roomId") String roomId);

}