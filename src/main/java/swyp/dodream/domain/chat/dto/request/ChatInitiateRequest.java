package swyp.dodream.domain.chat.dto.request;

import lombok.Data;

// '채팅하기' 버튼 클릭 시 (REST API)
@Data
public class ChatInitiateRequest {
    private Long postId;
    // senderId(memberId)는 @AuthenticationPrincipal을 통해 얻습니다.
}