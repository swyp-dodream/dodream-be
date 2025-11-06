package swyp.dodream.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.Suggestion;
import swyp.dodream.domain.post.dto.request.SuggestionRequest;
import swyp.dodream.domain.post.dto.response.SuggestionResponse;
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

    public SuggestionResponse createSuggestion(Long fromUserId, Long postId, SuggestionRequest request) {
        // 1. 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        // 2. 리더 권한 확인
        if (!post.getOwner().getId().equals(fromUserId)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "게시글 작성자만 제안을 보낼 수 있습니다.");
        }

        // 3. 중복 제안 방지
        if (suggestionRepository.existsByPostIdAndToUserId(postId, request.toUserId())) {
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
