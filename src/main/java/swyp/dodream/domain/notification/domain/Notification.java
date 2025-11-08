package swyp.dodream.domain.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long receiverId; // 알림 받을 유저

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String message;  // 화면에 보여줄 문자열

    @Column
    private Long targetPostId;  // 알림 클릭 시 이동할 대상(모집글 id 등)

    @Column
    private String targetPostTitle; // 알림과 관련된 모집글 제목

    @Column(nullable = false)
    private boolean isRead = false;

    // 신규 알림 생성용 생성자
    public Notification(Long id, Long receiverId, NotificationType type, String message,Long targetPostId, String targetPostTitle) {
        this.id = id;
        this.receiverId = receiverId;
        this.type = type;
        this.message = message;
        this.targetPostId = targetPostId;
        this.targetPostTitle = targetPostTitle;
        this.isRead = false;
    }

    // 알림 읽음 처리
    public void markAsRead() {
        this.isRead = true;
    }
}