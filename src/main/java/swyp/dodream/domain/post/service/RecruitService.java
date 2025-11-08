package swyp.dodream.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.application.repository.ApplicationRepository;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.master.repository.RoleRepository;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.matched.repository.MatchedRepository;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.Suggestion;
import swyp.dodream.domain.post.dto.res.RecruitApplicationDetailResponse;
import swyp.dodream.domain.post.dto.res.RecruitListResponse;
import swyp.dodream.domain.post.dto.res.RecruitUserResponse;
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
    private final RoleRepository roleRepository;

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

    /**
     * 지원 상세 조회
     */
    public RecruitApplicationDetailResponse getApplicationDetail(Long viewerUserId,
                                                                 Long postId,
                                                                 Long applicationId) {
        // 1. 글 존재 + 권한 체크
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if (!post.getOwner().getId().equals(viewerUserId)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "권한이 없습니다.");
        }

        // 2. 이 글에 대한 지원서인지 같이 확인해서 가져오기
        Application application = applicationRepository.findByIdAndPostId(applicationId, postId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "지원 내역을 찾을 수 없습니다."));

        Long applicantId = application.getApplicant().getId();

        // 3. 프로필 가져오기 (roles까지 필요하니 네가 만든 fetch 있는 거 쓰면 더 좋음)
        Profile profile = profileRepository.findWithAllByUserId(applicantId).orElse(null);

        // 4. 지원 시 선택한 직군/역할 가져오기
        Long appliedRoleId = application.getRole().getId();     // ← 엔티티 필드명에 맞춰서 바꿔
        String appliedRoleName = null;
        if (appliedRoleId != null) {
            appliedRoleName = roleRepository.findById(appliedRoleId)
                    .map(Role::getName)
                    .orElse(null);
        }

        // 5. 메시지
        String message = application.getMessage();

        // 6. 응답 조립
        return RecruitApplicationDetailResponse.builder()
                .applicationId(application.getId())
                .userId(applicantId)
                .nickname(profile != null ? profile.getNickname() : application.getApplicant().getName())
                .profileImage(application.getApplicant().getProfileImageUrl())
                .status(application.getStatus().name())
                .createdAt(application.getCreatedAt())
                .experience(profile != null && profile.getExperience() != null ? profile.getExperience().name() : null)
                .jobGroups(profile != null
                        ? profile.getRoles().stream().map(r -> r.getName()).toList()
                        : List.of())
                .appliedRoleId(appliedRoleId)
                .appliedRoleName(appliedRoleName)
                .message(message)
                .build();
    }
}