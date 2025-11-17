package swyp.dodream.domain.matched.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.application.repository.ApplicationRepository;
import swyp.dodream.domain.bookmark.repository.BookmarkRepository;
import swyp.dodream.domain.matched.dto.MatchedPostPageResponse;
import swyp.dodream.domain.matched.dto.MatchedPostResponse;
import swyp.dodream.domain.notification.service.NotificationService;
import swyp.dodream.domain.post.common.CancelBy;
import swyp.dodream.domain.post.common.CancelReasonCode;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.matched.dto.MatchingCancelRequest;
import swyp.dodream.domain.matched.repository.MatchedRepository;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.suggestion.domain.Suggestion;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.suggestion.repository.SuggestionRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchedService {

    private final MatchedRepository matchedRepository;
    private final PostRepository postRepository;
    private final ApplicationRepository applicationRepository;
    private final SnowflakeIdService snowflakeIdService;
    private final SuggestionRepository suggestionRepository;
    private final NotificationService notificationService;
    private final BookmarkRepository bookmarkRepository;

    // 정책 상수
    private static final int LEADER_CANCEL_LIMIT_PER_POST = 2; // 모집글 당 2회
    private static final int MEMBER_MONTHLY_CANCEL_LIMIT_AFTER_24H = 2; // 24시간 이후 월 2회

    /**
     * 내가 매칭된 글 목록 조회 (페이지네이션)
     *
     * @param userId 유저 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     */
    public MatchedPostPageResponse getMyMatched(Long userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "matchedAt"));

        Page<Matched> matchedPage = matchedRepository.findMatchedByUser(userId, pageable);

        List<MatchedPostResponse> contents = matchedPage.getContent().stream()
                .map(matched -> {
                    Long postId = matched.getPost().getId();
                    boolean bookmarked = bookmarkRepository.existsByUserIdAndPostId(userId, postId);
                    return MatchedPostResponse.from(matched, bookmarked);
                })
                .toList();

        return MatchedPostPageResponse.of(
                contents,
                matchedPage.getNumber(),
                matchedPage.getSize(),
                matchedPage.getTotalElements(),
                matchedPage.getTotalPages(),
                matchedPage.hasNext()
        );
    }

    /**
     *  매칭 취소
     * - 리더 또는 멤버가 매칭을 취소한다.
     * - 리더: 모집글 단위로 최대 2회 취소 가능
     * - 멤버: 매칭 24시간 내 무제한 / 이후 월 2회 제한
     */
    @Transactional
    public void cancelMatching(Long matchingId, Long requesterUserId, MatchingCancelRequest req) {
        // 매칭 존재 여부 확인
        Matched matched = matchedRepository.findById(matchingId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "매칭 내역을 찾을 수 없습니다."));

        // 리더/멤버 판별
        Long leaderUserId = postRepository.findOwnerUserIdByPostId(matched.getPost().getId())
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "모집글을 찾을 수 없습니다."));

        boolean isLeader = leaderUserId.equals(requesterUserId);
        boolean isMember = matched.getUser().getId().equals(requesterUserId);

        if (!isLeader && !isMember) {
            throw new CustomException(ExceptionType.UNAUTHORIZED, "매칭 취소 권한이 없습니다.");
        }

        // 이미 취소된 매칭인지 확인
        if (matched.isCanceled()) {
            throw new CustomException(ExceptionType.BAD_REQUEST_INVALID, "이미 취소된 매칭입니다.");
        }

        // 리더/멤버별 취소 제한 검사
        if (isLeader) {
            int used = matchedRepository.countLeaderCancelsForPost(matched.getPost().getId());
            if (used >= LEADER_CANCEL_LIMIT_PER_POST) {
                throw new CustomException(ExceptionType.FORBIDDEN, "해당 모집글의 매칭 취소 허용 횟수를 초과했습니다.");
            }
        } else { // 멤버
            if (!matched.isWithin24h()) {
                LocalDateTime start = YearMonth.now().atDay(1).atStartOfDay();
                LocalDateTime end = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
                int used = matchedRepository.countMemberCancelsInRange(matched.getUser().getId(), start, end);
                if (used >= MEMBER_MONTHLY_CANCEL_LIMIT_AFTER_24H) {
                    throw new CustomException(ExceptionType.FORBIDDEN, "이번 달 매칭 취소 허용 횟수를 초과했습니다.");
                }
            }
        }

        // 상태 변경
        CancelBy cancelBy = isLeader ? CancelBy.LEADER : CancelBy.MEMBER;
        CancelReasonCode reason = Optional.ofNullable(req.reasonCode()).orElse(CancelReasonCode.OTHER);

        matched.cancel(cancelBy, reason);
        matchedRepository.save(matched);
    }

    @Transactional
    public void acceptApplication(Long leaderId, Long postId, Long applicationId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if (!post.getOwner().getId().equals(leaderId)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "리더만 수락할 수 있습니다.");
        }

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "지원 내역을 찾을 수 없습니다."));

        if (matchedRepository.existsByPostIdAndUserId(postId, app.getApplicant().getId())) {
            throw new CustomException(ExceptionType.BAD_REQUEST_INVALID, "이미 매칭된 사용자입니다.");
        }

        Matched matched = Matched.builder()
                .id(snowflakeIdService.generateId())
                .post(post)
                .user(app.getApplicant())
                .application(app)
                .matchedAt(LocalDateTime.now())
                .isCanceled(false)
                .build();

        matchedRepository.save(matched);

        // 지원자에게만 매칭 알림 보내기
        notificationService.sendApplicationAcceptedToApplicant(
                app.getApplicant().getId(),
                post.getId(),
                post.getTitle(),
                post.getOwner().getName()
        );
    }

    @Transactional
    public void acceptSuggestion(Long userId, Long suggestionId) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "제안을 찾을 수 없습니다."));

        if (!suggestion.getToUser().getId().equals(userId)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "본인에게 온 제안만 수락할 수 있습니다.");
        }

        Post post = suggestion.getPost();

        // 중복 매칭 방지
        if (matchedRepository.existsByPostIdAndUserId(post.getId(), userId)) {
            throw new CustomException(ExceptionType.BAD_REQUEST_INVALID, "이미 매칭된 사용자입니다.");
        }

        Matched matched = Matched.builder()
                .id(snowflakeIdService.generateId())
                .post(post)
                .user(suggestion.getToUser())
                .application(null) // 제안 기반 매칭이라 application 없음
                .matchedAt(LocalDateTime.now())
                .isCanceled(false)
                .build();

        matchedRepository.save(matched);

        // 제안 상태 변경
        suggestion.markAsAccepted();
    }
}
