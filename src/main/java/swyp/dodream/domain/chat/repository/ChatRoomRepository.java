package swyp.dodream.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.chat.domain.ChatRoom;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 게시글, 리더, 멤버 기준으로 1:1 채팅방을 조회합니다.
     * (UNIQUE 제약조건을 활용)
     */
    Optional<ChatRoom> findByPostIdAndLeaderUserIdAndMemberUserId(Long postId, Long leaderId, Long memberId);
}