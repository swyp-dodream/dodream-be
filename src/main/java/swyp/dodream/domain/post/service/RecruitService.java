package swyp.dodream.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.Suggestion;
import swyp.dodream.domain.post.dto.res.RecruitListResponse;
import swyp.dodream.domain.post.dto.res.RecruitUserResponse;
import swyp.dodream.domain.application.repository.ApplicationRepository;
import swyp.dodream.domain.matched.repository.MatchedRepository;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.post.repository.SuggestionRepository;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitService {

    private final PostRepository postRepository;
    private final SuggestionRepository suggestionRepository;
    private final ApplicationRepository applicationRepository;
    private final MatchedRepository matchedRepository;
    private final ProfileRepository profileRepository;

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
            suggestions = suggestionRepository.findSuggestionsByPost(
                    postId, userId, PageRequest.of(0, size));
        } else {
            suggestions = suggestionRepository.findSuggestionsByPostAfterCursor(
                    postId, userId, cursor, PageRequest.of(0, size));
        }

        // 3. 제안받은 유저 id 모으기
        List<Long> targetUserIds = suggestions.getContent().stream()
                .map(s -> s.getToUser().getId())
                .toList();

        // 4. 프로필 한 번에 조회
        List<Profile> profiles = profileRepository.findByUserIdIn(targetUserIds);
        Map<Long, Profile> profileMap = profiles.stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));

        // 5. DTO 변환 (Suggestion + Profile)
        List<RecruitUserResponse> users = suggestions.getContent().stream()
                .map(s -> {
                    Long toUserId = s.getToUser().getId();
                    Profile profile = profileMap.get(toUserId);
                    return RecruitUserResponse.fromSuggestion(s, profile);
                })
                .toList();

        // 6. nextCursor 계산: 레포지토리가 s.id < :cursor 쓰니까 s.id로 내려간다
        Long nextCursor = suggestions.getContent().isEmpty()
                ? null
                : suggestions.getContent().get(suggestions.getContent().size() - 1).getId();

        return RecruitListResponse.builder()
                .users(users)
                .nextCursor(nextCursor)
                .hasNext(suggestions.hasNext())
                .build();
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

        // 3. 지원한 유저 id 모으기
        List<Long> targetUserIds = applications.getContent().stream()
                .map(a -> a.getApplicant().getId())
                .toList();

        // 4. 프로필 한 번에 조회
        List<Profile> profiles = profileRepository.findByUserIdIn(targetUserIds);
        Map<Long, Profile> profileMap = profiles.stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));

        // 5. DTO 변환
        List<RecruitUserResponse> users = applications.getContent().stream()
                .map(a -> {
                    Long applicantId = a.getApplicant().getId();
                    Profile profile = profileMap.get(applicantId);
                    return RecruitUserResponse.fromApplication(a, profile);
                })
                .toList();

        // 6. nextCursor 계산
        Long nextCursor = applications.getContent().isEmpty()
                ? null
                : applications.getContent().get(applications.getContent().size() - 1).getId();

        return RecruitListResponse.builder()
                .users(users)
                .nextCursor(nextCursor)
                .hasNext(applications.hasNext())
                .build();
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

        // 3. 멤버 유저 id 모으기
        List<Long> targetUserIds = members.getContent().stream()
                .map(m -> m.getUser().getId())
                .toList();

        // 4. 프로필 한 번에 조회
        List<Profile> profiles = profileRepository.findByUserIdIn(targetUserIds);
        Map<Long, Profile> profileMap = profiles.stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));

        // 5. DTO 변환
        List<RecruitUserResponse> users = members.getContent().stream()
                .map(m -> {
                    Long memberId = m.getUser().getId();
                    Profile profile = profileMap.get(memberId);
                    return RecruitUserResponse.fromMatched(m, profile);
                })
                .toList();

        // 6. nextCursor 계산
        Long nextCursor = members.getContent().isEmpty()
                ? null : members.getContent().get(members.getContent().size() - 1).getId();

        return RecruitListResponse.builder()
                .users(users)
                .nextCursor(nextCursor)
                .hasNext(members.hasNext())
                .build();
    }
}