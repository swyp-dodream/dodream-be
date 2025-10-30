package swyp.dodream.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.post.domain.Application;
import swyp.dodream.domain.post.domain.Matched;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.Suggestion;
import swyp.dodream.domain.post.dto.res.RecruitListResponse;
import swyp.dodream.domain.post.dto.res.RecruitUserResponse;
import swyp.dodream.domain.post.repository.ApplicationRepository;
import swyp.dodream.domain.post.repository.MatchedRepository;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.post.repository.SuggestionRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitService {

    private final PostRepository postRepository;
    private final SuggestionRepository suggestionRepository;
    private final ApplicationRepository applicationRepository;
    private final MatchedRepository matchedRepository;

    /**
     * 제안한 내역 조회
     */
    public RecruitListResponse getOffers(Long userId, Long postId, Long cursor, Integer size) {
        // 1. 게시글 검증 & 권한 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if (!post.getOwner().getId().equals(userId)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "권한이 없습니다.");
        }

        // 2. 제안 목록 조회
        Slice<Suggestion> suggestions;
        if (cursor == null) {
            // 최초 로드
            suggestions = suggestionRepository.findSuggestionsByPost(
                    postId, userId, PageRequest.of(0, size));
        } else {
            // 다음 페이지
            suggestions = suggestionRepository.findSuggestionsByPostAfterCursor(
                    postId, userId, cursor, PageRequest.of(0, size));
        }

        // 3. DTO 변환
        Slice<RecruitUserResponse> responsePage = suggestions
                .map(RecruitUserResponse::fromSuggestion);

        return RecruitListResponse.of(responsePage);
    }

    /**
     * 지원 내역 조회
     */
    public RecruitListResponse getApplications(Long userId, Long postId, Long cursor, Integer size) {
        // 1. 게시글 검증 & 권한 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if (!post.getOwner().getId().equals(userId)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "권한이 없습니다.");
        }

        // 2. 지원 목록 조회
        Slice<Application> applications;
        if (cursor == null) {
            applications = applicationRepository.findApplicationsByPost(
                    postId, PageRequest.of(0, size));
        } else {
            applications = applicationRepository.findApplicationsByPostAfterCursor(
                    postId, cursor, PageRequest.of(0, size));
        }

        // 3. DTO 변환
        Slice<RecruitUserResponse> responsePage = applications
                .map(RecruitUserResponse::fromApplication);

        return RecruitListResponse.of(responsePage);
    }

    /**
     * 멤버 내역 조회
     */
    public RecruitListResponse getMembers(Long userId, Long postId, Long cursor, Integer size) {
        // 1. 게시글 검증 & 권한 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if (!post.getOwner().getId().equals(userId)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "권한이 없습니다.");
        }

        // 2. 멤버 목록 조회
        Slice<Matched> members;
        if (cursor == null) {
            members = matchedRepository.findMembersByPost(
                    postId, PageRequest.of(0, size));
        } else {
            members = matchedRepository.findMembersByPostAfterCursor(
                    postId, cursor, PageRequest.of(0, size));
        }

        // 3. DTO 변환
        Slice<RecruitUserResponse> responsePage = members
                .map(RecruitUserResponse::fromMatched);

        return RecruitListResponse.of(responsePage);
    }
}