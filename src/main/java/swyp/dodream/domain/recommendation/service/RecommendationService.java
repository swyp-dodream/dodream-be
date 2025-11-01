package swyp.dodream.domain.recommendation.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.domain.application.repository.ApplicationRepository;
import swyp.dodream.domain.ai.service.EmbeddingService;
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

    private static final int DEFAULT_SIZE = 20;
    private static final int FINAL_LIMIT = 10;

    /**
     * 사용자에게 추천 게시글 목록 반환
     * @param userId 사용자 ID
     * @param cursor 커서 (무한 스크롤용)
     * @param size 페이지 크기
     * @return 추천 게시글 목록
     */
    public RecommendationListResponse recommendPosts(Long userId, Long cursor, Integer size) {
        log.info("게시글 추천 요청: userId={}, cursor={}, size={}", userId, cursor, size);

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

            // 4. Qdrant에서 유사 게시글 검색 (Top-30)
            List<Long> similarPostIds = vectorRepository.get().searchSimilar(userEmbedding, DEFAULT_SIZE);
            log.info("유사 게시글 검색 완료: {}개", similarPostIds.size());

            // 5. 필터링 및 상세 정보 조회
            List<RecommendationPostResponse> recommendations = filterAndEnrichPosts(
                    similarPostIds, userId
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
            List<Long> postIds, Long userId
    ) {
        List<RecommendationPostResponse> result = new ArrayList<>();

        for (Long postId : postIds) {
            Post post = postRepository.findById(postId)
                    .orElse(null);

            if (post == null) {
                continue;
            }

            // 필터링: 모집 중만, 본인 게시글 제외, 이미 지원한 글 제외
            if (!shouldIncludePost(post, userId)) {
                continue;
            }

            // 유사도 점수 계산 (간단한 로직: 추후 개선 가능)
            double similarity = 0.85; // TODO: 실제 유사도 계산

            RecommendationPostResponse response = RecommendationPostResponse.from(post, similarity);
            result.add(response);
        }

        return result;
    }

    /**
     * 게시글을 추천 목록에 포함할지 판단
     */
    private boolean shouldIncludePost(Post post, Long userId) {
        // 1. 모집 중인가?
        if (post.getStatus() != PostStatus.RECRUITING) {
            return false;
        }

        // 2. 마감일이 지나지 않았는가?
        if (post.getDeadlineAt() != null && post.getDeadlineAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 3. 본인이 작성한 글인가?
        if (post.getOwner().getId().equals(userId)) {
            return false;
        }

        // 4. 이미 지원한 글인가?
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && applicationRepository.existsByPostAndApplicant(post, user)) {
            return false;
        }

        return true;
    }
}

