package swyp.dodream.domain.home.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.dto.res.PostSummaryResponse;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.post.repository.PostSpecification;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final PostRepository postRepository;

    public Page<PostSummaryResponse> getHomePosts(
            ProjectType type,
            List<String> roles,
            List<String> techs,
            List<String> interests,
            ActivityMode activityMode,
            boolean onlyRecruiting,
            String sort,
            Pageable pageable
    ) {
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

        return posts.map(PostSummaryResponse::fromEntity);
    }
}