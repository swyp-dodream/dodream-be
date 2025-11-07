package swyp.dodream.domain.chat.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import swyp.dodream.domain.chat.domain.ChatParticipant;
import swyp.dodream.domain.chat.domain.ChatParticipantId;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatParticipantId> { // [수정] Long -> ChatParticipantId

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.userId = :userId")
    List<ChatParticipant> findAllByUserId(@Param("userId") Long userId);
}