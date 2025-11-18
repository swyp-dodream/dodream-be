package swyp.dodream.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swyp.dodream.domain.chat.domain.ChatRoom;
import swyp.dodream.domain.chat.domain.ReadStatus;
import swyp.dodream.domain.user.domain.User;

import java.util.List;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {

    // 이 메서드들은 이제 ReadStatus.user 필드를 기준으로 동작합니다.
    List<ReadStatus> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    Long countByChatRoomAndUserAndIsReadFalse(ChatRoom chatRoom, User user);

    // messageRead 기능을 위한 새 메서드
    List<ReadStatus> findByChatRoomAndUserAndIsReadFalse(ChatRoom chatRoom, User user);
}