package swyp.dodream.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.notification.domain.Notification;
import swyp.dodream.domain.notification.domain.NotificationType;
import swyp.dodream.domain.notification.dto.NotificationPayload;
import swyp.dodream.domain.notification.infra.RedisNotificationPublisher;
import swyp.dodream.domain.notification.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SnowflakeIdService snowflakeIdService;
    private final RedisNotificationPublisher redisPublisher;   // 이제 실제로 주입해서 씀

    /**
     * 리더가 유저에게 제안을 보낼 때 알림 전송
     */
    @Transactional
    public void sendProposalNotificationToUser(Long receiverId, Long postId, String leaderName, String postTitle) {
        // 동일 알림 중복 방지 (유저 + 타입 + 모집글 기본키)
        boolean exists = notificationRepository.existsByReceiverIdAndTypeAndTargetPostId(
                receiverId,
                NotificationType.PROPOSAL_SENT,
                postId
        );
        if (exists) {
            return;
        }

        Long id = snowflakeIdService.generateId();
        String msg = leaderName + "님이 제안을 보냈습니다. " + "\'" + postTitle + "\'로 이동하여 지원을 하시면 매칭이 가능합니다.";

        Notification notification = new Notification(
                id,
                receiverId,
                NotificationType.PROPOSAL_SENT,
                msg,
                postId,
                postTitle
        );

        notificationRepository.save(notification);

        // 실시간 SSE 전파를 위한 Redis Pub
        redisPublisher.publish(
                new NotificationPayload(receiverId, NotificationType.PROPOSAL_SENT, msg, postId)
        );
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        if (!notification.getReceiverId().equals(userId)) {
            throw new IllegalStateException("본인 알림만 읽음 처리할 수 있습니다.");
        }

        notification.markAsRead();
    }
}