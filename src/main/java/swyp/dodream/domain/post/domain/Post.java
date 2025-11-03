package swyp.dodream.domain.post.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import swyp.dodream.common.entity.BaseEntity;
import swyp.dodream.domain.master.domain.InterestKeyword;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.DurationPeriod;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.user.domain.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE post SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false") // 항상 deleted=false 조건만 적용됨 (soft delete)
public class Post extends BaseEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType projectType;  // PROJECT, STUDY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityMode activityMode;  // ONLINE, OFFLINE, HYBRID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DurationPeriod duration;

    private LocalDateTime deadlineAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.RECRUITING;

    private String title;

    @Lob
    private String content;

    private Boolean deleted = false;

    // 연관 관계 (Builder에서 제외)
    @OneToMany(mappedBy = "post")
    private List<PostField> fields = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostStack> stacks = new ArrayList<>();

    @OneToMany(mappedBy = "post")
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

    @Builder
    public Post(Long id, User owner, ProjectType projectType, ActivityMode activityMode, 
                DurationPeriod duration, LocalDateTime deadlineAt, PostStatus status, 
                String title, String content) {
        this.id = id;
        this.owner = owner;
        this.projectType = projectType;
        this.activityMode = activityMode;
        this.duration = duration;
        this.deadlineAt = deadlineAt;
        this.status = status != null ? status : PostStatus.RECRUITING;
        this.title = title;
        this.content = content;
    }

    public void increaseViewCount() {
        if (this.postView != null) {
            this.postView.increment(); // PostView 내부에서 count += 1 하는 메서드
        }
    }

    public void closeRecruitment() {
        this.status = PostStatus.COMPLETED;
    }

    public void updateBasicInfo(
            String title,
            String content,
            ActivityMode activityMode,
            DurationPeriod duration,
            LocalDateTime deadlineAt,
            ProjectType projectType
    ) {
        this.title = title;
        this.content = content;
        this.activityMode = activityMode;
        this.duration = duration;
        this.deadlineAt = deadlineAt;
        this.projectType = projectType;
    }

    public void updateTitle(String title) { this.title = title; }
    public void updateContent(String content) { this.content = content; }
    public void updateActivityMode(ActivityMode mode) { this.activityMode = mode; }
    public void updateDuration(DurationPeriod duration) { this.duration = duration; }
    public void updateDeadlineAt(LocalDateTime deadline) { this.deadlineAt = deadline; }
    public void updateProjectType(ProjectType type) { this.projectType = type; }
    public void updateStatus(PostStatus status) { this.status = status; }

}