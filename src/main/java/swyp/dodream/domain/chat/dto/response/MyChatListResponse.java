package swyp.dodream.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyChatListResponse {
    private Long roomId;
    private String roomName;
    private Long unReadCount;
}