package swyp.dodream.domain.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantId implements Serializable {

    private String chatRoomId;  // String
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChatParticipantId that = (ChatParticipantId) o;
        return Objects.equals(chatRoomId, that.chatRoomId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatRoomId, userId);
    }
}