package swyp.dodream.domain.notification.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import swyp.dodream.domain.notification.dto.NotificationPayload;

@Component
@RequiredArgsConstructor
public class RedisNotificationPublisher {

    private final RedisTemplate<String, Object> notificationRedisTemplate;
    private final ChannelTopic notificationTopic;

    public void publish(NotificationPayload payload) {
        notificationRedisTemplate.convertAndSend(notificationTopic.getTopic(), payload);
    }
}