package swyp.dodream.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.notification.domain.NotificationType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {
    private Long receiverId;
    private NotificationType type;
    private String message;
    private Long targetPostId;
}