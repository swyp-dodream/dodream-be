package swyp.dodream.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.chat.domain.ChatParticipant;
import swyp.dodream.domain.chat.domain.ChatParticipantId;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatParticipantId> {
    List<ChatParticipant> findAllByUserId(Long userId);
}