package swyp.dodream.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;
import swyp.dodream.domain.notification.domain.Notification;
import swyp.dodream.domain.notification.domain.NotificationType;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {
    private String id;
    private Long senderId;
    private Long receiverId;
    private NotificationType type;
    private String message;
    private Long targetPostId;
    private String targetPostTitle;
    private boolean isRead;
    private Integer profileImageCode;
    private LocalDateTime createdAt;

    public static NotificationResponse of(Notification notification, Integer profileImageCode) {
        return NotificationResponse.builder()
                .id(String.valueOf(notification.getId()))
                .senderId(notification.getSenderId())
                .receiverId(notification.getReceiverId())
                .type(notification.getType())
                .message(notification.getMessage())
                .targetPostId(notification.getTargetPostId())
                .targetPostTitle(notification.getTargetPostTitle())
                .isRead(notification.isRead())
                .profileImageCode(profileImageCode)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}