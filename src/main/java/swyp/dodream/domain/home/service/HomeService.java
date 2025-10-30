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
import swyp.dodream.domain.post.dto.PostSummaryResponse;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.post.repository.PostSpecification;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final PostRepository postRepository;

    // ==============================
    // 홈 목록 조회 (필터 + 검색 + 정렬 + 페이지네이션)
    // ==============================
    public Page<PostSummaryResponse> getHomePosts(
            ProjectType type,
            String keyword,
            List<String> roles,
            List<String> techs,
            List<String> interests,
            ActivityMode activityMode,
            boolean onlyRecruiting,
            String sort,
            Pageable pageable
    ) {
        // 초기 스펙
        Specification<Post> spec = Specification.where(null);

        // 프로젝트 유형 필터
        if (type != null && type != ProjectType.ALL) {
            spec = spec.and(PostSpecification.hasType(type));
        }

        // 키워드 검색
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(PostSpecification.containsKeyword(keyword));
        }

        // 다중 선택된 직군 필터
        if (roles != null && !roles.isEmpty()) {
            spec = spec.and(PostSpecification.hasAnyRole(roles));
        }

        // 다중 선택된 기술 필터
        if (techs != null && !techs.isEmpty()) {
            spec = spec.and(PostSpecification.hasAnyTech(techs));
        }

        // 스터디(STUDY)는 interest 필터 무시
        if (type != ProjectType.STUDY && interests != null && !interests.isEmpty()) {
            spec = spec.and(PostSpecification.hasAnyInterest(interests));
        }

        // 활동 방식 필터
        if (activityMode != null) {
            spec = spec.and(PostSpecification.hasActivityMode(activityMode));
        }

        // 모집 중 필터
        if (onlyRecruiting) {
            spec = spec.and(PostSpecification.hasStatus(PostStatus.RECRUITING));
        }

        // 정렬 기준 설정
        Sort sorting = switch (sort.toLowerCase()) {
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "recruitEndDate");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sorting);

        // DB 조회
        Page<Post> posts = postRepository.findAll(spec, sortedPageable);

        // DTO 매핑
        return posts.map(PostSummaryResponse::fromEntity);
    }
}