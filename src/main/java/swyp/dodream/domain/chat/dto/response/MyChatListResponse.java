package swyp.dodream.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyChatListResponse {
    private String roomId;
    private String roomName;
    private Long unReadCount;
    private String topicId;
    private String leaderId;
    private String memberId;
    private String myRole;
    private Long postId;

    private String lastMessage;           // 마지막 메시지 내용
    private LocalDateTime lastMessageAt;  // 마지막 메시지 시간

    private Integer leaderProfileImageCode;   // 리더 프로필 이미지 코드
    private Integer memberProfileImageCode;   // 멤버 프로필 이미지 코드
}