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

    /**
     * 제안받은 유저가 실제 지원했을 때 글작성자에게 알림 전송
     */
    @Transactional
    public void sendProposalAppliedNotification(Long leaderId, Long postId, String applicantName, String postTitle) {
        boolean exists = notificationRepository.existsByReceiverIdAndTypeAndTargetPostId(
                leaderId,
                NotificationType.PROPOSAL_APPLIED,
                postId
        );
        if (exists) return;

        Long id = snowflakeIdService.generateId();
        String msg = "이전에 제안했던 " + applicantName + "님이 '" + postTitle + "'에 지원했습니다.";

        Notification notification = new Notification(
                id,
                leaderId,
                NotificationType.PROPOSAL_APPLIED,
                msg,
                postId,
                postTitle
        );

        notificationRepository.save(notification);

        // 실시간 알림 전송
        redisPublisher.publish(
                new NotificationPayload(leaderId, NotificationType.PROPOSAL_APPLIED, msg, postId)
        );
    }

    /**
     * 지원자가 수락되었을 때(매칭된 경우) -> 지원자에게 알려주기
     */
    @Transactional
    public void sendApplicationAcceptedToApplicant(Long applicantId,
                                                   Long postId,
                                                   String postTitle,
                                                   String leaderName) {

        boolean exists = notificationRepository
                .existsByReceiverIdAndTypeAndTargetPostId(applicantId, NotificationType.APPLICATION_ACCEPTED, postId);
        if (exists) return;

        Long id = snowflakeIdService.generateId();
        String msg = leaderName + "님이 '" + postTitle + "' 지원을 수락했어요.";

        Notification notification = new Notification(
                id,
                applicantId,
                NotificationType.APPLICATION_ACCEPTED,
                msg,
                postId,
                postTitle
        );
        notificationRepository.save(notification);

        redisPublisher.publish(
                new NotificationPayload(applicantId, NotificationType.APPLICATION_ACCEPTED, msg, postId)
        );
    }

    /**
     * 지원자가 수락되었을 때(매칭된 경우) -> 글 작성자(리더)에게도 알려주기
     */
    @Transactional
    public void sendApplicationAcceptedToLeader(Long leaderId,
                                                Long postId,
                                                String applicantName,
                                                String postTitle) {

        boolean exists = notificationRepository
                .existsByReceiverIdAndTypeAndTargetPostId(leaderId, NotificationType.APPLICATION_ACCEPTED, postId);
        if (exists) return;

        Long id = snowflakeIdService.generateId();
        String msg = applicantName + "님과 매칭이 완료됐어요.";

        Notification notification = new Notification(
                id,
                leaderId,
                NotificationType.APPLICATION_ACCEPTED,
                msg,
                postId,
                postTitle
        );
        notificationRepository.save(notification);

        redisPublisher.publish(
                new NotificationPayload(leaderId, NotificationType.APPLICATION_ACCEPTED, msg, postId)
        );
    }

    /**
     * 다른 사람이 자신에게 피드백을 달아주었을 때 알림
     */
    @Transactional
    public void sendFeedbackWrittenNotification(Long receiverId, Long postId, String postTitle) {

        Long id = snowflakeIdService.generateId();
        String msg = "익명의 팀원이 '" + postTitle + "'에 피드백을 작성했습니다.";

        Notification notification = new Notification(
                id,
                receiverId,                       // 글 작성자
                NotificationType.FEEDBACK_WRITTEN,
                msg,
                postId,
                postTitle
        );

        notificationRepository.save(notification);

        // SSE 푸시
        redisPublisher.publish(
                new NotificationPayload(receiverId, NotificationType.FEEDBACK_WRITTEN, msg, postId)
        );
    }

    @Transactional
    public void sendBookmarkDeadlineNotification(Long receiverId, Long postId, String postTitle) {
        boolean exists = notificationRepository.existsByReceiverIdAndTypeAndTargetPostId(
                receiverId,
                NotificationType.BOOKMARK_DEADLINE,
                postId
        );
        if (exists) return;

        Long id = snowflakeIdService.generateId();
        String msg = "북마크 해둔 '" + postTitle + "' 모집글이 오늘 마감됩니다. 잊지말고 확인해보세요!";

        Notification notification = new Notification(
                id,
                receiverId,
                NotificationType.BOOKMARK_DEADLINE,
                msg,
                postId,
                postTitle
        );

        notificationRepository.save(notification);

        redisPublisher.publish(
                new NotificationPayload(receiverId, NotificationType.BOOKMARK_DEADLINE, msg, postId)
        );
    }

    @Transactional
    public void sendReviewActivated(Long receiverId, Long postId, String postTitle) {
        boolean exists = notificationRepository.existsByReceiverIdAndTypeAndTargetPostId(
                receiverId,
                NotificationType.REVIEW_ACTIVATED,
                postId
        );
        if (exists) return;

        Long id = snowflakeIdService.generateId();
        String msg = "'" + postTitle + "' 모집글에 대한 팀원 피드백을 오늘부터 작성할 수 있어요.";

        Notification notification = new Notification(
                id,
                receiverId,
                NotificationType.REVIEW_ACTIVATED,
                msg,
                postId,
                postTitle
        );

        notificationRepository.save(notification);

        redisPublisher.publish(
                new NotificationPayload(receiverId, NotificationType.REVIEW_ACTIVATED, msg, postId)
        );
    }


}