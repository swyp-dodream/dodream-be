package swyp.dodream.domain.home.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.domain.bookmark.repository.BookmarkRepository;
import swyp.dodream.domain.home.dto.HomeResponse;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.dto.response.PostSummaryResponse;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.post.repository.PostSpecification;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final BookmarkRepository bookmarkRepository;

    public HomeResponse getHomePosts(
            Long userId,
            ProjectType type,
            List<String> roles,
            List<String> techs,
            List<String> interests,
            ActivityMode activityMode,
            boolean onlyRecruiting,
            String sort,
            Pageable pageable
    ) {
        // 사용자 프로필 이미지 코드 조회
        Integer userProfileImageCode = null;
        if (userId != null) {
            userProfileImageCode = profileRepository.findByUserId(userId)
                    .map(Profile::getProfileImageCode)
                    .orElse(1);  // 기본값 1
        }

        // 초기 스펙
        Specification<Post> spec = PostSpecification.notDeleted();

        // 프로젝트 유형 필터
        if (type != null && type != ProjectType.ALL) {
            spec = spec.and(PostSpecification.hasType(type));
        }

        // 다중 선택된 직군 필터 (빈 문자열 제거)
        if (roles != null && !roles.isEmpty()) {
            List<String> filteredRoles = roles.stream()
                    .filter(role -> role != null && !role.isBlank())
                    .toList(); // .collect(Collectors.toList()); (Java 11)
            if (!filteredRoles.isEmpty()) {
                spec = spec.and(PostSpecification.hasAnyRole(filteredRoles));
            }
        }

        // 다중 선택된 기술 필터 (빈 문자열 제거)
        if (techs != null && !techs.isEmpty()) {
            List<String> filteredTechs = techs.stream()
                    .filter(tech -> tech != null && !tech.isBlank())
                    .toList(); // .collect(Collectors.toList()); (Java 11)
            if (!filteredTechs.isEmpty()) {
                spec = spec.and(PostSpecification.hasAnyTech(filteredTechs));
            }
        }

        // 관심 분야 필터 (빈 문자열 제거)
        if (interests != null && !interests.isEmpty()) {
            List<String> filteredInterests = interests.stream()
                    .filter(interest -> interest != null && !interest.isBlank())
                    .toList(); // .collect(Collectors.toList()); (Java 11)
            if (!filteredInterests.isEmpty()) {
                spec = spec.and(PostSpecification.hasAnyInterest(filteredInterests));
            }
        }

        // 활동 방식 필터
        if (activityMode != null) {
            spec = spec.and(PostSpecification.hasActivityMode(activityMode));
        }

        // 모집 중 필터
        if (onlyRecruiting) {
            spec = spec.and(PostSpecification.hasStatus(PostStatus.RECRUITING));
        }

        Sort sorting = switch (sort.toLowerCase()) {
            case "popular" -> Sort.by(Sort.Direction.DESC, "postView.views");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "deadlineAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sorting);

        Page<Post> posts = postRepository.findAll(spec, sortedPageable);

        // 작성자들의 userId 목록 추출
        List<Long> ownerIds = posts.getContent().stream()
                .map(post -> post.getOwner().getId())
                .distinct()
                .toList();

        // 한 번에 Profile 조회 (N+1 방지)
        Map<Long, Profile> profileMap = profileRepository.findByUserIdIn(ownerIds).stream()
                .collect(Collectors.toMap(
                        Profile::getUserId,
                        profile -> profile
                ));

        // 북마크 정보 조회
        Set<Long> bookmarkedPostIds = userId != null
                ? new HashSet<>(bookmarkRepository.findPostIdsByUserId(userId))
                : new HashSet<>();

        // DTO 변환
        Page<PostSummaryResponse> postResponses = posts.map(post -> {
            Profile ownerProfile = profileMap.get(post.getOwner().getId());
            boolean isBookmarked = bookmarkedPostIds.contains(post.getId());

            return PostSummaryResponse.fromEntity(
                    post,
                    ownerProfile != null ? ownerProfile.getProfileImageCode() : 1,
                    ownerProfile != null ? ownerProfile.getNickname() : post.getOwner().getName(),
                    isBookmarked
            );
        });

        return HomeResponse.builder()
                .userProfileImageCode(userProfileImageCode)
                .posts(postResponses)
                .build();
    }
}