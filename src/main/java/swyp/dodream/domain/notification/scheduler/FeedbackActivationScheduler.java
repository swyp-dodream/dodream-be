package swyp.dodream.domain.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.matched.repository.MatchedRepository;
import swyp.dodream.domain.notification.service.NotificationService;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.repository.PostRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedbackActivationScheduler {

    private final PostRepository postRepository;
    private final MatchedRepository matchedRepository;
    private final NotificationService notificationService;

    /**
     * 매일 00:10에 '한 달 전에 마감된' 모집글의 팀원들에게
     * "피드백 작성 가능" 알림을 보내기
     */
    @Scheduled(cron = "0 10 0 * * *") // 다른 스케줄러와 겹치지 않게 하기 위해 00시 10분으로 설정
    @Transactional
    public void sendFeedbackActivationNotifications() {
        // 한 달 전 날짜
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);

        // 1달 전 그 날에 마감된 글 찾기: 1달 전 00:00 ~ 23:59:59
        LocalDateTime start = oneMonthAgo.atStartOfDay();
        LocalDateTime end = oneMonthAgo.atTime(23, 59, 59);

        List<Post> posts = postRepository.findByDeadlineAtBetween(start, end);

        for (Post post : posts) {
            // 이 글에 매칭된 팀원 전부 가져오기
            List<Matched> members = matchedRepository.findAllByPostId(post.getId());
            for (Matched member : members) {
                if (member.isCanceled()) continue;

                notificationService.sendReviewActivated(
                        member.getUser().getId(),
                        post.getId(),
                        post.getTitle()
                );
            }
        }

        log.info("[FeedbackActivationScheduler] {} 마감 글의 팀원들에게 피드백 활성화 알림 전송 완료. 글 수: {}", oneMonthAgo, posts.size());
    }
}
