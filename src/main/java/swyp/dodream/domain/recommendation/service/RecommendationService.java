package swyp.dodream.domain.recommendation.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.domain.application.repository.ApplicationRepository;
import swyp.dodream.domain.ai.service.EmbeddingService;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.recommendation.dto.RecommendationListResponse;
import swyp.dodream.domain.recommendation.dto.RecommendationPostResponse;
import swyp.dodream.domain.recommendation.repository.VectorRepository;
import swyp.dodream.domain.recommendation.util.TextExtractor;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 게시글 추천 서비스
 * 사용자 프로필 기반으로 유사한 게시글 추천
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final Optional<EmbeddingService> embeddingService;
    private final Optional<VectorRepository> vectorRepository;

    private static final int DEFAULT_SIZE = 10;  // Qdrant에서 검색할 개수 (필터링 후 5개 확보를 위해 여유있게)
    private static final int FINAL_LIMIT = 5;    // 최종 반환할 개수

    /**
     * 사용자에게 추천 게시글 목록 반환
     * @param userId 사용자 ID
     * @param cursor 커서 (무한 스크롤용)
     * @param size 페이지 크기
     * @param projectType 프로젝트 타입 (PROJECT, STUDY, ALL 또는 null)
     * @return 추천 게시글 목록
     */
    public RecommendationListResponse recommendPosts(Long userId, Long cursor, Integer size, ProjectType projectType) {
        log.info("게시글 추천 요청: userId={}, cursor={}, size={}, projectType={}", userId, cursor, size, projectType);

        // 1. 사용자 프로필 조회
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("프로필을 찾을 수 없습니다."));

        // 2. 임베딩 서비스 확인
        if (embeddingService.isEmpty() || vectorRepository.isEmpty()) {
            log.warn("벡터 DB 미사용 환경 - 추천 기능 비활성화");
            return RecommendationListResponse.of(new ArrayList<>(), null, false);
        }

        // 3. 프로필 → 텍스트 → 임베딩
        String profileText = TextExtractor.extractFromProfile(profile);
        log.debug("프로필 텍스트: {}", profileText);

        try {
            float[] userEmbedding = embeddingService.get().embed(profileText);
            log.info("사용자 임베딩 생성 완료: {}차원", userEmbedding.length);

            // 4. Qdrant에서 유사 게시글 검색 (Top-10, 필터링 후 최종 5개 반환)
            Map<Long, Double> postSimilarities = vectorRepository.get().searchSimilar(userEmbedding, DEFAULT_SIZE);
            log.info("유사 게시글 검색 완료: {}개, postSimilarities={}", postSimilarities.size(), postSimilarities);

            // 5. 필터링 및 상세 정보 조회
            List<RecommendationPostResponse> recommendations = filterAndEnrichPosts(
                    postSimilarities, userId, projectType
            );

            // 6. 커서 기반 페이징
            int limit = (size != null && size > 0) ? size : FINAL_LIMIT;
            List<RecommendationPostResponse> paginated = recommendations.stream()
                    .limit(limit)
                    .toList();

            // 7. nextCursor 계산
            Long nextCursor = paginated.isEmpty() ? null :
                    paginated.get(paginated.size() - 1).postId();
            boolean hasNext = paginated.size() == limit;

            return RecommendationListResponse.of(paginated, nextCursor, hasNext);

        } catch (Exception e) {
            log.error("게시글 추천 실패", e);
            // 임베딩 실패 시 빈 리스트 반환
            return RecommendationListResponse.of(new ArrayList<>(), null, false);
        }
    }

    /**
     * 검색된 게시글들을 필터링하고 상세 정보 추가
     */
    private List<RecommendationPostResponse> filterAndEnrichPosts(
            Map<Long, Double> postSimilarities, Long userId, ProjectType projectType
    ) {
        List<RecommendationPostResponse> result = new ArrayList<>();
        log.info("필터링 시작: postSimilarities 개수={}, userId={}, projectType={}", postSimilarities.size(), userId, projectType);

        for (Map.Entry<Long, Double> entry : postSimilarities.entrySet()) {
            Long postId = entry.getKey();
            Double similarity = entry.getValue();

            Post post = postRepository.findById(postId)
                    .orElse(null);

            if (post == null) {
                log.debug("게시글을 찾을 수 없음: postId={}", postId);
                continue;
            }

            // 필터링: 모집 중만, 본인 게시글 제외, 이미 지원한 글 제외, projectType 필터
            if (!shouldIncludePost(post, userId, projectType)) {
                log.debug("게시글 필터링 제외: postId={}, status={}, ownerId={}, postProjectType={}", 
                        postId, post.getStatus(), post.getOwner().getId(), post.getProjectType());
                continue;
            }

            // 실제 유사도 점수 사용 (Qdrant에서 반환된 Cosine Similarity 점수)
            RecommendationPostResponse response = RecommendationPostResponse.from(post, similarity);
            result.add(response);
            log.debug("게시글 추가: postId={}, similarity={}", postId, similarity);
        }

        log.info("필터링 완료: 최종 추천 게시글 개수={}", result.size());
        return result;
    }

    /**
     * 게시글을 추천 목록에 포함할지 판단
     */
    private boolean shouldIncludePost(Post post, Long userId, ProjectType projectType) {
        // 1. 삭제된 게시글인가?
        if (Boolean.TRUE.equals(post.getDeleted())) {
            log.debug("게시글 제외: 삭제됨 - postId={}", post.getId());
            return false;
        }

        // 2. 모집 중인가? (모집 마감된 게시글 제외)
        if (post.getStatus() != PostStatus.RECRUITING) {
            log.debug("게시글 제외: 모집 중이 아님 (모집 마감) - postId={}, status={}", post.getId(), post.getStatus());
            return false;
        }

        // 3. 마감일이 지나지 않았는가?
        if (post.getDeadlineAt() != null && post.getDeadlineAt().isBefore(LocalDateTime.now())) {
            log.debug("게시글 제외: 마감일 지남 - postId={}, deadlineAt={}", post.getId(), post.getDeadlineAt());
            return false;
        }

        // 4. 본인이 작성한 글인가?
        if (post.getOwner().getId().equals(userId)) {
            log.debug("게시글 제외: 본인 게시글 - postId={}, ownerId={}", post.getId(), post.getOwner().getId());
            return false;
        }

        // 5. 이미 지원한 글인가?
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && applicationRepository.existsByPostAndApplicant(post, user)) {
            log.debug("게시글 제외: 이미 지원함 - postId={}, userId={}", post.getId(), userId);
            return false;
        }

        // 6. projectType 필터링 (null이거나 ALL이면 모든 타입 포함)
        if (projectType != null && projectType != ProjectType.ALL) {
            if (post.getProjectType() != projectType) {
                log.debug("게시글 제외: projectType 불일치 - postId={}, postProjectType={}, filterProjectType={}", 
                        post.getId(), post.getProjectType(), projectType);
                return false;
            }
        }

        return true;
    }
}

