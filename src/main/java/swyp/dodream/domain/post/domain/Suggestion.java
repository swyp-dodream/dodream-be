package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import lombok.Getter;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "suggestion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "to_user_id"}))
@Getter
public class Suggestion {

    @Id
    private Long id;

    @Column(length = 1000, nullable = false)
    private String suggestionMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    private LocalDateTime createdAt = LocalDateTime.now();
}
