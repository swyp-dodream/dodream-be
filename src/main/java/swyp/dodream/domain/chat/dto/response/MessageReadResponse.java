package swyp.dodream.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageReadResponse {
    private int readCount; // 이번 요청으로 읽힌 unread 개수
}
