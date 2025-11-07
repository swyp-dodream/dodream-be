package swyp.dodream.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.chat.dto.ChatMessageDto;

import java.util.List;

// '채팅하기' 버튼 응답
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatInitiateResponse {
    private String roomId;  // 기존 방이 있으면 ID, 없으면 null
    private String topicId; // 구독해야 할 WebSocket 토픽 주소

    // ⭐ Long → String 변환!
    private String leaderId;  // 상대방 (리더)
    private String memberId;  // 본인 (멤버)

    private String myRole; // "LEADER" or "MEMBER"

    private List<ChatMessageDto> history; // 기존 대화 내역
}