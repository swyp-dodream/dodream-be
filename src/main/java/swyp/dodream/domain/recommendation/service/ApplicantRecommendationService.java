package swyp.dodream.domain.recommendation.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.domain.ai.service.EmbeddingService;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.application.repository.ApplicationRepository;
import swyp.dodream.domain.master.domain.ApplicationStatus;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.domain.PostRole;
import swyp.dodream.domain.post.domain.PostStack;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.recommendation.dto.RecommendedApplicantListResponse;
import swyp.dodream.domain.recommendation.dto.RecommendedApplicantResponse;
import swyp.dodream.domain.recommendation.repository.VectorRepository;
import swyp.dodream.domain.recommendation.util.TextExtractor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 지원 유저 AI 추천 서비스
 * 모집글 기반으로 적합한 지원자를 AI로 선별하여 추천
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicantRecommendationService {

    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationRepository applicationRepository;
    private final Optional<EmbeddingService> embeddingService;
    private final Optional<VectorRepository> vectorRepository;

    private static final int MAX_RECOMMENDATIONS = 3;  // 최대 추천 개수

    /**
     * 지원 유저 AI 추천
     *
     * @param userId 리더 사용자 ID
     * @param postId 모집글 ID
     * @param roleId 추천받을 직군 ID (null이면 전체)
     * @return 추천된 지원자 목록 (최대 3명)
     */
    public RecommendedApplicantListResponse recommendApplicants(Long userId, Long postId, Long roleId) {
        log.info("=== 지원 유저 AI 추천 시작 ===");
        log.info("사용자 ID: {}, 게시글 ID: {}, 직군 ID: {}", userId, postId, roleId);

        // 1. 모집글 조회 및 권한 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 게시글의 리더만 추천을 받을 수 있습니다.");
        }

        // 2. 지원자 목록 조회 (APPLIED 상태만)
        List<Application> applications = applicationRepository.findApplicationsByPost(postId, null).getContent();
        List<Application> filteredApplications = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPLIED)
                .filter(app -> roleId == null || app.getRole().getId().equals(roleId))
                .collect(Collectors.toList());

        log.info("필터링된 지원자 수: {}", filteredApplications.size());

        if (filteredApplications.isEmpty()) {
            log.info("추천할 지원자가 없습니다.");
            return RecommendedApplicantListResponse.builder()
                    .applicants(List.of())
                    .totalCount(0)
                    .build();
        }

        // 3. AI 추천이 가능한지 확인
        if (embeddingService.isEmpty() || vectorRepository.isEmpty()) {
            log.warn("Embedding 서비스 또는 Vector 저장소가 활성화되지 않았습니다. 기본 정렬 반환");
            return getDefaultRecommendations(filteredApplications);
        }

        // 4. 게시글 텍스트 추출
        String postText = TextExtractor.extractPostText(post);
        log.info("게시글 텍스트 추출 완료 (길이: {})", postText.length());

        try {
            // 5. 게시글 임베딩 생성
            float[] postEmbedding = embeddingService.get().generateEmbedding(postText);
            log.info("게시글 임베딩 생성 완료");

            // 6. 각 지원자의 유사도 계산
            List<ApplicantSimilarity> similarities = new ArrayList<>();

            for (Application application : filteredApplications) {
                try {
                    Profile profile = profileRepository.findByUserId(application.getApplicant().getId())
                            .orElse(null);

                    if (profile == null) {
                        log.warn("지원자 ID {}의 프로필을 찾을 수 없습니다.", application.getApplicant().getId());
                        continue;
                    }

                    // 프로필 + 지원 메시지 텍스트 생성
                    String applicantText = TextExtractor.extractProfileText(profile) + "\n지원 메시지: " + application.getMessage();
                    float[] applicantEmbedding = embeddingService.get().generateEmbedding(applicantText);

                    // 코사인 유사도 계산
                    double similarity = cosineSimilarity(postEmbedding, applicantEmbedding);
                    log.debug("지원자 {} 유사도: {}", profile.getNickname(), similarity);

                    similarities.add(new ApplicantSimilarity(application, profile, similarity));

                } catch (Exception e) {
                    log.error("지원자 처리 중 오류 발생: {}", e.getMessage(), e);
                }
            }

            // 7. 유사도 기준으로 정렬하고 상위 3명 선택
            List<ApplicantSimilarity> topApplicants = similarities.stream()
                    .sorted(Comparator.comparingDouble(ApplicantSimilarity::similarity).reversed())
                    .limit(MAX_RECOMMENDATIONS)
                    .collect(Collectors.toList());

            log.info("추천 지원자 수: {}", topApplicants.size());

            // 8. 응답 생성
            List<RecommendedApplicantResponse> recommendedApplicants = topApplicants.stream()
                    .map(applicantSim -> {
                        List<String> tags = generateTags(post, applicantSim.profile(), applicantSim.application());
                        return RecommendedApplicantResponse.builder()
                                .applicationId(applicantSim.application().getId())
                                .profileId(applicantSim.profile().getId())
                                .nickname(applicantSim.profile().getNickname())
                                .profileImageUrl(getProfileImageUrl(applicantSim.profile()))
                                .role(applicantSim.application().getRole().getName())
                                .career(applicantSim.profile().getExperience().getDescription())
                                .applicationMessage(applicantSim.application().getMessage())
                                .similarity(applicantSim.similarity())
                                .tags(tags)
                                .build();
                    })
                    .collect(Collectors.toList());

            return RecommendedApplicantListResponse.builder()
                    .applicants(recommendedApplicants)
                    .totalCount(recommendedApplicants.size())
                    .build();

        } catch (Exception e) {
            log.error("AI 추천 처리 중 오류 발생: {}", e.getMessage(), e);
            return getDefaultRecommendations(filteredApplications);
        }
    }

    /**
     * AI 추천이 불가능한 경우 기본 정렬 반환 (최신순)
     */
    private RecommendedApplicantListResponse getDefaultRecommendations(List<Application> applications) {
        List<RecommendedApplicantResponse> defaultApplicants = applications.stream()
                .limit(MAX_RECOMMENDATIONS)
                .map(app -> {
                    Profile profile = profileRepository.findByUserId(app.getApplicant().getId()).orElse(null);
                    if (profile == null) {
                        return null;
                    }

                    return RecommendedApplicantResponse.builder()
                            .applicationId(app.getId())
                            .profileId(profile.getId())
                            .nickname(profile.getNickname())
                            .profileImageUrl(getProfileImageUrl(profile))
                            .role(app.getRole().getName())
                            .career(profile.getExperience().getDescription())
                            .applicationMessage(app.getMessage())
                            .similarity(0.0)
                            .tags(List.of())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return RecommendedApplicantListResponse.builder()
                .applicants(defaultApplicants)
                .totalCount(defaultApplicants.size())
                .build();
    }

    /**
     * 추천 태그 생성 (최대 2개)
     */
    private List<String> generateTags(Post post, Profile profile, Application application) {
        List<String> tags = new ArrayList<>();

        // 1. 기술스택 매칭
        Set<Long> postTechSkillIds = post.getStacks().stream()
                .map(PostStack::getTechSkill)
                .map(ts -> ts.getId())
                .collect(Collectors.toSet());

        Set<Long> profileTechSkillIds = profile.getTechSkills().stream()
                .map(ts -> ts.getId())
                .collect(Collectors.toSet());

        Set<Long> commonTechSkills = new HashSet<>(postTechSkillIds);
        commonTechSkills.retainAll(profileTechSkillIds);

        if (!commonTechSkills.isEmpty()) {
            tags.add("나와맞는기술스택");
        }

        // 2. 선호하는 분야 (관심 분야 1~2순위)
        Set<Long> postFieldIds = post.getFields().stream()
                .map(pf -> pf.getInterestKeyword().getId())
                .collect(Collectors.toSet());

        Set<Long> profileInterestIds = profile.getInterestKeywords().stream()
                .limit(2)  // 1~2순위만
                .map(ik -> ik.getId())
                .collect(Collectors.toSet());

        Set<Long> commonInterests = new HashSet<>(postFieldIds);
        commonInterests.retainAll(profileInterestIds);

        if (!commonInterests.isEmpty() && tags.size() < 2) {
            tags.add("선호하는분야");
        }

        // 3. 선호하는 활동방식
        if (post.getActivityMode().name().equals(profile.getActivityMode().name()) && tags.size() < 2) {
            tags.add("선호하는활동방식");
        }

        // 4. 내글과 비슷한 키워드 (기본)
        if (tags.isEmpty()) {
            tags.add("내글과비슷한키워드");
        }

        // 최대 2개로 제한
        return tags.stream().limit(2).collect(Collectors.toList());
    }

    /**
     * 프로필 이미지 URL 생성
     */
    private String getProfileImageUrl(Profile profile) {
        // 프로필 이미지 코드를 URL로 변환 (실제 구현은 프론트엔드와 협의 필요)
        return "/images/profile/" + profile.getProfileImageCode() + ".png";
    }

    /**
     * 코사인 유사도 계산
     */
    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("벡터 길이가 일치하지 않습니다.");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 지원자와 유사도를 함께 저장하는 내부 클래스
     */
    private record ApplicantSimilarity(Application application, Profile profile, double similarity) {
    }
}

