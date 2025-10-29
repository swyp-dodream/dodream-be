package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import swyp.dodream.domain.master.domain.InterestKeyword;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Table(name = "post")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE post SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false") // 항상 deleted=false 조건만 적용됨 (soft delete)
public class Post {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType projectType;  // PROJECT, STUDY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityMode activityMode;  // ONLINE, OFFLINE, HYBRID

    private String durationText;

    private LocalDateTime deadlineAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.RECRUITING;

    private String title;

    @Lob
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 연관 관계
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostField> fields = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostStack> stacks = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostRole> roleRequirements = new ArrayList<>();

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private PostView postView;

//    PostField가 InterestKeyword와 매핑을 담당하므로 주석 처리
//    @ManyToMany
//    @JoinTable(
//            name = "post_interest",
//            joinColumns = @JoinColumn(name = "post_id"),
//            inverseJoinColumns = @JoinColumn(name = "interest_keyword_id")
//    )
//    private List<InterestKeyword> interests = new ArrayList<>();

    public void increaseViewCount() {
        if (this.postView != null) {
            this.postView.increment(); // PostView 내부에서 count += 1 하는 메서드
        }
    }

    public void closeRecruitment() {
        this.status = PostStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBasicInfo(
            String title,
            String content,
            ActivityMode activityMode,
            String durationText,
            LocalDateTime deadlineAt,
            ProjectType projectType
    ) {
        this.title = title;
        this.content = content;
        this.activityMode = activityMode;
        this.durationText = durationText;
        this.deadlineAt = deadlineAt;
        this.projectType = projectType;
    }

    public void updateStatus(PostStatus status) {
        this.status = status;
    }
}