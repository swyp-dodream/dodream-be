package swyp.dodream.domain.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.domain.bookmark.domain.Bookmark;
import swyp.dodream.domain.bookmark.repository.BookmarkRepository;
import swyp.dodream.domain.notification.service.NotificationService;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.repository.PostRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookmarkDeadlineScheduler {

    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;
    private final NotificationService notificationService;

    /**
     * 매일 자정에 오늘 마감 모집글 북마크 유저에게 알림 전송
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 매일 00:00
    @Transactional
    public void sendBookmarkDeadlineNotifications() {
        LocalDate today = LocalDate.now();

        List<Post> posts = postRepository.findByDeadlineAtBetween(
                today.atStartOfDay(),
                today.atTime(23, 59, 59)
        );

        for (Post post : posts) {
            List<Bookmark> bookmarks = bookmarkRepository.findByPost(post);
            for (Bookmark bookmark : bookmarks) {
                notificationService.sendBookmarkDeadlineNotification(
                        bookmark.getUser().getId(),
                        post.getId(),
                        post.getTitle()
                );
            }
        }

        log.info("[BookmarkDeadlineScheduler] 오늘({}) 마감 모집글 알림 전송 완료", today);
    }
}