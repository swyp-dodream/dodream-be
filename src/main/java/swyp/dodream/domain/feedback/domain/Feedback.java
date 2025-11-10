package swyp.dodream.domain.feedback.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feedback",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "from_user_id", "to_user_id"}))
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;  // 피드백 작성자는 익명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;    // 피드백 받는 사람

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackType feedbackType;  // POSITIVE / NEGATIVE

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "feedback_options", joinColumns = @JoinColumn(name = "feedback_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "option_value")
    private List<FeedbackOption> options = new ArrayList<>();  // 상세 선택지 (최대 3개, 자유 선택)

    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public Feedback(Long id, Post post, User fromUser, User toUser,
                    FeedbackType feedbackType, List<FeedbackOption> options) {
        this.id = id;
        this.post = post;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.feedbackType = feedbackType;
        this.options = options != null ? options : new ArrayList<>();
    }

    /**
     * 피드백 옵션 검증 (최대 3개만)
     */
    public void validateOptions() {
        if (options.size() > 3) {
            throw new IllegalArgumentException("피드백 옵션은 최대 3개까지만 선택할 수 있습니다.");
        }
    }
}