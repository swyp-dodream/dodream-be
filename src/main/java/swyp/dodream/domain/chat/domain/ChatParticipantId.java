package swyp.dodream.domain.chat.domain;

import java.io.Serializable;
import java.util.Objects;

public class ChatParticipantId implements Serializable {

    private Long chatRoom;
    private Long userId;

    public ChatParticipantId() {
    }

    public ChatParticipantId(Long chatRoom, Long userId) {
        this.chatRoom = chatRoom;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatParticipantId that = (ChatParticipantId) o;
        return Objects.equals(chatRoom, that.chatRoom) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatRoom, userId);
    }
}