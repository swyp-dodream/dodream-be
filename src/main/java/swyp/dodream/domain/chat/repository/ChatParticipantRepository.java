package swyp.dodream.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.chat.domain.ChatParticipant;
import swyp.dodream.domain.chat.domain.ChatParticipantId;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatParticipantId> {
}