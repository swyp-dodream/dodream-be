package swyp.dodream.domain.chat.service;

import swyp.dodream.domain.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

// (1) redis pub/sub 채널에 publish 해주는 기능과
// (2) redis에서 들어온 메시지를 websocket topic으로 다시 브로드캐스트하는 중간 브로커 역할
public class RedisPubSubService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate; // redis publish 할 때 쓰는 템플릿
    private final SimpMessageSendingOperations messageTemplate; // websocket 에게 메시지를 push 하는 스프링 템플릿

    // @Qualifier("chatPubSub")로 지정된 redisTemplate bean을 주입받는다
    public RedisPubSubService(@Qualifier("chatPubSub") StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messageTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageTemplate = messageTemplate;
    }

    // 특정 채널로 메시지를 redis pub/sub 채널에 publish 한다.
    // publish 하면 redis가 해당 채널을 listen 중인 서버들에게 퍼뜨린다.
    public void publish(String channel, String message){
        stringRedisTemplate.convertAndSend(channel, message);
    }

    @Override
    // 이 메서드는 redis에서 메시지가 들어올 때 자동으로 호출된다.
    // message = 실제 redis에서 온 메시지
    // pattern = 구독중인 패턴 (channel 이름의 pattern)
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody()); // redis에서 온 메시지 raw byte[] -> String(JSON)으로 변환.
        ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱용 objectMapper 생성
        try {
            ChatMessageDto chatMessageDto = objectMapper.readValue(payload, ChatMessageDto.class); // redis에서 받은 JSON 문자열을 ChatMessageDto로 역직렬화
            messageTemplate.convertAndSend("/topic/"+chatMessageDto.getRoomId(), chatMessageDto); // websocket으로 브로드캐스트. 구독 주소 = /topic/{roomId}
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
