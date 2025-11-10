package swyp.dodream.domain.notification.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import swyp.dodream.domain.notification.dto.NotificationPayload;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SseEmitterPool sseEmitterPool;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            NotificationPayload payload =
                    objectMapper.readValue(message.getBody(), NotificationPayload.class);
            sseEmitterPool.sendToUser(payload.getReceiverId(), payload);
        }  catch (Exception e) {
            log.error("알림 메시지 처리 실패", e);
        }
    }
}
