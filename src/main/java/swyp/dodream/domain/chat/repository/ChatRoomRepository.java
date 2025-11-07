package swyp.dodream.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.chat.domain.ChatRoom;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    Optional<ChatRoom> findByPostIdAndLeaderUserIdAndMemberUserId(
            Long postId, Long leaderId, Long memberId);
}