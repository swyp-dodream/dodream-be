package swyp.dodream.domain.matched.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.post.common.CancelBy;
import swyp.dodream.domain.post.common.CancelReasonCode;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.matched.dto.MatchingCancelRequest;
import swyp.dodream.domain.matched.repository.MatchedRepository;
import swyp.dodream.domain.post.repository.PostRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchedService {

    private final MatchedRepository matchedRepository;
    private final PostRepository postRepository;

    // 정책 상수
    private static final int LEADER_CANCEL_LIMIT_PER_POST = 2; // 모집글 당 2회
    private static final int MEMBER_MONTHLY_CANCEL_LIMIT_AFTER_24H = 2; // 24시간 이후 월 2회

    /**
     * ✅ 매칭 취소
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
}
