package swyp.dodream.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.notification.domain.Notification;
import swyp.dodream.domain.notification.domain.NotificationType;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    boolean existsByReceiverIdAndTypeAndTargetPostId(Long receiverId, NotificationType type, Long targetPostId);
}