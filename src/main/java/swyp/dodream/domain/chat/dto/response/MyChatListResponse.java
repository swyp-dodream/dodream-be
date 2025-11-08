package swyp.dodream.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}