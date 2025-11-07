package swyp.dodream.domain.recommendation.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.domain.ai.service.EmbeddingService;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.recommendation.dto.RecommendationProfileListResponse;
import swyp.dodream.domain.recommendation.dto.RecommendationProfileResponse;
import swyp.dodream.domain.recommendation.repository.VectorRepository;
import swyp.dodream.domain.recommendation.util.TextExtractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 프로필 추천 서비스
 * 게시글 기반으로 유사한 프로필을 추천
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileRecommendationService {

    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final Optional<EmbeddingService> embeddingService;
    private final Optional<VectorRepository> vectorRepository;

    private static final int DEFAULT_SIZE = 10;  // Qdrant에서 검색할 개수 (필터링 후 5개 확보를 위해 여유있게)
    private static final int FINAL_LIMIT = 5;    // 최종 반환할 개수

    /**
     * 게시글 기반으로 추천 프로필 목록 반환
     * @param postId 게시글 ID
     * @param cursor 커서 (무한 스크롤용)
     * @param size 페이지 크기
     * @return 추천 프로필 목록
     */
    public RecommendationProfileListResponse recommendProfiles(Long postId, Long cursor, Integer size) {
        log.info("프로필 추천 요청: postId={}, cursor={}, size={}", postId, cursor, size);

        // 1. 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        // 2. 임베딩 서비스 확인
        if (embeddingService.isEmpty() || vectorRepository.isEmpty()) {
            log.warn("벡터 DB 미사용 환경 - 추천 기능 비활성화");
            return RecommendationProfileListResponse.of(new ArrayList<>(), null, false);
        }

        // 3. 게시글 → 텍스트 → 임베딩
        String postText = TextExtractor.extractFromPost(post);
        log.debug("게시글 텍스트: {}", postText);

        try {
            float[] postEmbedding = embeddingService.get().embed(postText);
            log.info("게시글 임베딩 생성 완료: {}차원", postEmbedding.length);

            // 4. Qdrant에서 유사 프로필 검색 (Top-10, 필터링 후 최종 5개 반환)
            List<Long> similarProfileIds = vectorRepository.get().searchSimilarProfiles(postEmbedding, DEFAULT_SIZE);
            log.info("유사 프로필 검색 완료: {}개", similarProfileIds.size());

            // 5. 필터링 및 상세 정보 조회
            List<RecommendationProfileResponse> recommendations = filterAndEnrichProfiles(
                    similarProfileIds, post, postId
            );

            // 6. 커서 기반 페이징
            int limit = (size != null && size > 0) ? size : FINAL_LIMIT;
            List<RecommendationProfileResponse> paginated = recommendations.stream()
                    .limit(limit)
                    .toList();

            // 7. nextCursor 계산
            Long nextCursor = paginated.isEmpty() ? null :
                    paginated.get(paginated.size() - 1).profileId();
            boolean hasNext = paginated.size() == limit;

            return RecommendationProfileListResponse.of(paginated, nextCursor, hasNext);

        } catch (Exception e) {
            log.error("프로필 추천 실패", e);
            // 임베딩 실패 시 빈 리스트 반환
            return RecommendationProfileListResponse.of(new ArrayList<>(), null, false);
        }
    }

    /**
     * 검색된 프로필들을 필터링하고 상세 정보 추가
     */
    private List<RecommendationProfileResponse> filterAndEnrichProfiles(
            List<Long> profileIds, Post post, Long postId
    ) {
        List<RecommendationProfileResponse> result = new ArrayList<>();

        for (Long profileId : profileIds) {
            Profile profile = profileRepository.findById(profileId)
                    .orElse(null);

            if (profile == null) {
                continue;
            }

            // 필터링: 공개 프로필만, 게시글 작성자 제외
            if (!shouldIncludeProfile(profile, post)) {
                continue;
            }

            // 태그 생성
            List<String> tags = generateTags(profile, post);

            // 유사도 점수 계산 (간단한 로직: 추후 개선 가능)
            double similarity = 0.85; // TODO: 실제 유사도 계산

            RecommendationProfileResponse response = RecommendationProfileResponse.from(profile, similarity, tags);
            result.add(response);
        }

        return result;
    }

    /**
     * 프로필을 추천 목록에 포함할지 판단
     */
    private boolean shouldIncludeProfile(Profile profile, Post post) {
        // 1. 공개 프로필인가?
        if (!profile.getIsPublic()) {
            return false;
        }

        // 2. 게시글 작성자인가?
        if (profile.getUserId().equals(post.getOwner().getId())) {
            return false;
        }

        return true;
    }

    /**
     * 추천 태그 생성 (최대 2개)
     * 우선순위: #선호하는활동방식 > #사용하는기술스택 > #선호하는분야
     */
    private List<String> generateTags(Profile profile, Post post) {
        List<String> tags = new ArrayList<>();

        // #선호하는활동방식: 모집글의 활동방식과 프로필의 활동방식이 일치
        // Post.ActivityMode (ONLINE, OFFLINE, HYBRID)와 Profile.ActivityMode (온라인, 오프라인, 하이브리드) 매핑
        if (post.getActivityMode() != null && profile.getActivityMode() != null) {
            ActivityMode postMode = post.getActivityMode();
            swyp.dodream.domain.profile.enums.ActivityMode profileMode = profile.getActivityMode();
            
            boolean matches = (postMode == ActivityMode.ONLINE && profileMode == swyp.dodream.domain.profile.enums.ActivityMode.온라인) ||
                             (postMode == ActivityMode.OFFLINE && profileMode == swyp.dodream.domain.profile.enums.ActivityMode.오프라인) ||
                             (postMode == ActivityMode.HYBRID && profileMode == swyp.dodream.domain.profile.enums.ActivityMode.하이브리드);
            
            if (matches) {
                tags.add("#선호하는활동방식");
            }
        }

        // #사용하는기술스택: 모집글의 기술스택과 프로필의 기술스택이 교집합
        Set<String> postTechStacks = post.getStacks().stream()
                .map(stack -> stack.getTechSkill().getName())
                .collect(Collectors.toSet());
        Set<String> profileTechStacks = profile.getTechSkills().stream()
                .map(tech -> tech.getName())
                .collect(Collectors.toSet());
        
        Set<String> commonTechStacks = new HashSet<>(postTechStacks);
        commonTechStacks.retainAll(profileTechStacks);
        if (!commonTechStacks.isEmpty()) {
            tags.add("#사용하는기술스택");
        }

        // #선호하는분야: 모집글의 분야와 프로필의 관심 분야가 교집합
        Set<String> postFields = post.getFields().stream()
                .map(field -> field.getInterestKeyword().getName())
                .collect(Collectors.toSet());
        Set<String> profileInterests = profile.getInterestKeywords().stream()
                .map(keyword -> keyword.getName())
                .collect(Collectors.toSet());
        
        Set<String> commonFields = new HashSet<>(postFields);
        commonFields.retainAll(profileInterests);
        if (!commonFields.isEmpty()) {
            tags.add("#선호하는분야");
        }

        // 최대 2개만 반환 (우선순위 순서대로)
        return tags.stream()
                .limit(2)
                .toList();
    }
}

