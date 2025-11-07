package swyp.dodream.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.chat.domain.ChatMessage;
import swyp.dodream.domain.chat.domain.ChatRoom;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 특정 채팅방의 모든 메시지를 시간순으로 조회합니다.
     */
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);
}