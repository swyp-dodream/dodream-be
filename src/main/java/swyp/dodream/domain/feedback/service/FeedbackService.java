package swyp.dodream.domain.feedback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.feedback.domain.Feedback;
import swyp.dodream.domain.feedback.domain.FeedbackOption;
import swyp.dodream.domain.feedback.dto.request.FeedbackCreateRequest;
import swyp.dodream.domain.feedback.dto.response.*;
import swyp.dodream.domain.feedback.repository.FeedbackRepository;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.notification.service.NotificationService;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.matched.repository.MatchedRepository;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final PostRepository postRepository;
    private final MatchedRepository matchedRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 피드백 옵션 전체 목록 조회
     * (프론트엔드에서 선택지 보여주기 위함)
     */
    public List<FeedbackOptionResponse> getAllOptions() {
        return Arrays.stream(FeedbackOption.values())
                .map(FeedbackOptionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 피드백 작성 가능한 게시글 목록 조회
     * (매칭된 글 중 모집마감 후 1달 지난 것들)
     *
     * @param userId 유저 ID
     * @return 피드백 작성 가능한 게시글 목록
     */
    public List<FeedbackAvailablePostResponse> getAvailablePosts(Long userId) {
        // 1. 내가 매칭된 게시글 조회
        List<Matched> matchedList = matchedRepository.findMatchedByUser(
                userId, PageRequest.of(0, 100)
        ).getContent();

        // 2. 모집마감 후 1달 지난 게시글 필터링
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        return matchedList.stream()
                .map(Matched::getPost)
                .filter(post -> post.getDeadlineAt() != null &&
                        post.getDeadlineAt().isBefore(oneMonthAgo))
                .map(post -> {
                    // 3. 해당 게시글의 모든 팀원 조회
                    List<Matched> members = matchedRepository.findMembersByPost(
                            post.getId(), PageRequest.of(0, 100)
                    ).getContent();

                    // 4. 본인 제외 & 피드백 작성 여부 확인
                    List<FeedbackMemberResponse> memberResponses = members.stream()
                            .filter(m -> !m.getUser().getId().equals(userId))
                            .map(m -> {
                                boolean alreadyWritten = feedbackRepository.existsByPostAndFromUserAndToUser(
                                        post.getId(), userId, m.getUser().getId()
                                );
                                return FeedbackMemberResponse.of(m.getUser(), alreadyWritten);
                            })
                            .collect(Collectors.toList());

                    return FeedbackAvailablePostResponse.of(post, true, memberResponses);
                })
                .collect(Collectors.toList());
    }

    /**
     * 피드백 작성
     *
     * @param fromUserId 작성자 ID
     * @param request 피드백 작성 요청
     * @return 피드백 작성 완료 응답
     */
    @Transactional
    public FeedbackCreateResponse createFeedback(Long fromUserId, FeedbackCreateRequest request) {
        // 1. 게시글 검증
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        // 2. 모집마감 후 1달 지났는지 확인
        if (post.getDeadlineAt() == null ||
                post.getDeadlineAt().isAfter(LocalDateTime.now().minusMonths(1))) {
            throw new CustomException(ExceptionType.BAD_REQUEST, "피드백 작성 가능 기간이 아닙니다. (모집마감 후 1달 이후부터 가능)");
        }

        // 3. 유저 검증
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        User toUser = userRepository.findById(request.toUserId())
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "피드백 대상 사용자를 찾을 수 없습니다."));

        // 4. 본인에게 피드백 작성 방지
        if (fromUserId.equals(request.toUserId())) {
            throw new CustomException(ExceptionType.BAD_REQUEST, "본인에게는 피드백을 작성할 수 없습니다.");
        }

        // 5. 작성자가 해당 게시글에 매칭되어 있는지 확인
        boolean isFromUserMatched = matchedRepository.findMembersByPost(
                        request.postId(), PageRequest.of(0, 100)
                ).getContent().stream()
                .anyMatch(m -> m.getUser().getId().equals(fromUserId));

        if (!isFromUserMatched) {
            throw new CustomException(ExceptionType.FORBIDDEN, "해당 프로젝트의 팀원만 피드백을 작성할 수 있습니다.");
        }

        // 6. 피드백 받을 사람도 해당 게시글에 매칭되어 있는지 확인
        boolean isToUserMatched = matchedRepository.findMembersByPost(
                        request.postId(), PageRequest.of(0, 100)
                ).getContent().stream()
                .anyMatch(m -> m.getUser().getId().equals(request.toUserId()));

        if (!isToUserMatched) {
            throw new CustomException(ExceptionType.BAD_REQUEST, "피드백 대상자가 해당 프로젝트의 팀원이 아닙니다.");
        }

        // 7. 중복 피드백 방지
        if (feedbackRepository.existsByPostAndFromUserAndToUser(
                request.postId(), fromUserId, request.toUserId())) {
            throw new CustomException(ExceptionType.BAD_REQUEST, "이미 해당 팀원에게 피드백을 작성했습니다.");
        }

        // 8. 피드백 생성
        Feedback feedback = Feedback.builder()
                .post(post)
                .fromUser(fromUser)
                .toUser(toUser)
                .feedbackType(request.feedbackType())
                .options(request.options())
                .build();

        // 9. 검증 (옵션 최대 3개)
        feedback.validateOptions();

        // 10. 저장
        Feedback saved = feedbackRepository.save(feedback);

        // 11. 알림 - 익명의 팀원이 피드백을 작성하면 피드백을 받는 대상이 알림을 받기
        notificationService.sendFeedbackWrittenNotification(
                request.toUserId(),          // 피드백 받은 사람
                post.getId(),
                post.getTitle()
        );

        return FeedbackCreateResponse.of(saved.getId());
    }

    /**
     * 내가 받은 피드백 조회 (익명)
     *
     * @param userId 유저 ID
     * @return 받은 피드백 목록
     */
    public List<FeedbackReceivedResponse> getReceivedFeedbacks(Long userId) {
        List<Feedback> feedbacks = feedbackRepository.findByToUser(userId);

        return feedbacks.stream()
                .map(FeedbackReceivedResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 게시글에서 내가 작성한 피드백 목록 조회
     *
     * @param userId 유저 ID
     * @param postId 게시글 ID
     * @return 내가 작성한 피드백 목록
     */
    public List<FeedbackReceivedResponse> getMyFeedbacksByPost(Long userId, Long postId) {
        // 1. 게시글 검증
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        // 2. 내가 매칭된 게시글인지 확인
        boolean isMatched = matchedRepository.findMembersByPost(
                        postId, PageRequest.of(0, 100)
                ).getContent().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));

        if (!isMatched) {
            throw new CustomException(ExceptionType.FORBIDDEN, "해당 프로젝트의 팀원만 조회할 수 있습니다.");
        }

        // 3. 내가 작성한 피드백 조회
        List<Feedback> feedbacks = feedbackRepository.findByPostAndFromUser(postId, userId);

        return feedbacks.stream()
                .map(FeedbackReceivedResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeedbackReceivedResponse> getReceivedFeedbacksByPost(Long userId, Long postId) {
        // 1) 유저/게시글 로드
        User me = userRepository.findByIdAndStatusTrue(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.UNAUTHORIZED, "탈퇴한 사용자입니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        // 2) 권한: 작성자이거나 or 멤버(매칭되고 취소되지 않음)
        boolean isOwner = post.getOwner().getId().equals(userId);
        boolean isMember = matchedRepository.existsByPostAndUserAndIsCanceledFalse(post, me);
        if (!(isOwner || isMember)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "해당 게시글의 팀원이 아니거나 권한이 없습니다.");
        }

        // 3) “내가 받은” 피드백만 조회 (to_user == me)
        List<Feedback> feedbacks = feedbackRepository
                .findByPostAndToUserOrderByCreatedAtDesc(post, me);

        return feedbacks.stream()
                .map(FeedbackReceivedResponse::from)
                .toList();
    }
}