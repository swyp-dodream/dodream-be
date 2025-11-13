package swyp.dodream.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.notification.service.NotificationService;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.Suggestion;
import swyp.dodream.domain.post.dto.req.SuggestionRequest;
import swyp.dodream.domain.post.dto.res.SuggestionResponse;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.post.repository.SuggestionRepository;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SnowflakeIdService snowflakeIdService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public boolean hasActiveSuggestion(Long fromUserId, Long postId, Long toUserId) {
        return suggestionRepository.existsActiveByPostIdAndToUserId(postId, toUserId);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Long> getActiveSuggestionId(Long postId, Long toUserId) {
        return suggestionRepository.findActiveByPostIdAndToUserId(postId, toUserId).map(Suggestion::getId);
    }
    public SuggestionResponse createSuggestion(Long fromUserId, Long postId, SuggestionRequest request) {
        // 1. 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        // 2. 리더 권한 확인
        if (!post.getOwner().getId().equals(fromUserId)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "게시글 작성자만 제안을 보낼 수 있습니다.");
        }

        if (suggestionRepository.existsActiveByPostIdAndToUserId(postId, request.toUserId())) {
            throw new CustomException(ExceptionType.BAD_REQUEST_INVALID, "이미 해당 회원에게 제안이 전송되었습니다.");
        }

        // 4. 대상 회원 조회
        User toUser = userRepository.findById(request.toUserId())
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "제안 대상 회원을 찾을 수 없습니다."));

        // 5. 제안 생성
        Suggestion suggestion = new Suggestion(
                snowflakeIdService.generateId(),
                request.suggestionMessage(),
                post,
                post.getOwner(),   // fromUser
                toUser,
                LocalDateTime.now()
        );

        suggestionRepository.save(suggestion);

        // 게시글 작성자가 일반 유저에게 제안하는 경우 알림이 가도록 하는 로직 추가!
        notificationService.sendProposalNotificationToUser(
                toUser.getId(),    // 알림 받을 사람
                post.getId(),     // 관련 게시글
                post.getOwner().getName(), // 보낸 사람 이름
                post.getTitle()   // 게시글 제목
        );

        return SuggestionResponse.builder()
                .id(suggestion.getId())
                .postId(postId)
                .toUserId(toUser.getId())
                .fromUserId(fromUserId)
                .suggestionMessage(suggestion.getSuggestionMessage())
                .createdAt(suggestion.getCreatedAt())
                .build();
    }


    public void cancelSuggestion(Long suggestionId, Long userId) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "제안 내역을 찾을 수 없습니다."));

        // 제안 보낸 사람만 취소 가능
        if (!suggestion.getFromUser().getId().equals(userId)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "본인이 보낸 제안만 취소할 수 있습니다.");
        }

        // 이미 취소된 경우 예외
        if (suggestion.getWithdrawnAt() != null) {
            throw new CustomException(ExceptionType.BAD_REQUEST_INVALID, "이미 취소된 제안입니다.");
        }

        suggestion.withdraw(); // 취소 처리
    }
}