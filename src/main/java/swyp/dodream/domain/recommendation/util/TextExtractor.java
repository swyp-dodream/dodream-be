package swyp.dodream.domain.recommendation.util;

import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.profile.domain.Profile;

import java.util.stream.Collectors;

/**
 * 텍스트 추출 유틸리티
 * Profile, Post 엔티티를 임베딩 가능한 텍스트로 변환
 */
public class TextExtractor {

    /**
     * 프로필을 텍스트로 변환
     * 포맷: "기술: 스택1 스택2 ... / 직군: 역할1 역할2 ... / 관심사: 키워드1 키워드2 ... / 활동방식: 온라인"
     */
    public static String extractFromProfile(Profile profile) {
        StringBuilder sb = new StringBuilder();

        // 기술 스택
        if (profile.getTechSkills() != null && !profile.getTechSkills().isEmpty()) {
            String techs = profile.getTechSkills().stream()
                    .map(tech -> tech.getName())
                    .collect(Collectors.joining(" "));
            sb.append("기술: ").append(techs).append(" ");
        }

        // 직군
        if (profile.getRoles() != null && !profile.getRoles().isEmpty()) {
            String roles = profile.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.joining(" "));
            sb.append("직군: ").append(roles).append(" ");
        }

        // 관심 키워드
        if (profile.getInterestKeywords() != null && !profile.getInterestKeywords().isEmpty()) {
            String interests = profile.getInterestKeywords().stream()
                    .map(interest -> interest.getName())
                    .collect(Collectors.joining(" "));
            sb.append("관심사: ").append(interests).append(" ");
        }

        // 활동 방식
        if (profile.getActivityMode() != null) {
            sb.append("활동방식: ").append(profile.getActivityMode().name());
        }

        return sb.toString().trim();
    }

    /**
     * 게시글을 텍스트로 변환
     * 포맷: "제목 / 기술: 스택1 스택2 ... / 직군: 역할1 역할2 ... / 분야: 키워드1 키워드2 ... / 내용: ..."
     */
    public static String extractFromPost(Post post) {
        StringBuilder sb = new StringBuilder();

        // 제목
        if (post.getTitle() != null) {
            sb.append(post.getTitle()).append(" ");
        }

        // 기술 스택
        if (post.getStacks() != null && !post.getStacks().isEmpty()) {
            String techs = post.getStacks().stream()
                    .map(stack -> stack.getTechSkill().getName())
                    .collect(Collectors.joining(" "));
            sb.append("기술: ").append(techs).append(" ");
        }

        // 모집 직군
        if (post.getRoleRequirements() != null && !post.getRoleRequirements().isEmpty()) {
            String roles = post.getRoleRequirements().stream()
                    .map(req -> req.getRole().getName())
                    .collect(Collectors.joining(" "));
            sb.append("직군: ").append(roles).append(" ");
        }

        // 관심 분야
        if (post.getFields() != null && !post.getFields().isEmpty()) {
            String fields = post.getFields().stream()
                    .map(field -> field.getInterestKeyword().getName())
                    .collect(Collectors.joining(" "));
            sb.append("분야: ").append(fields).append(" ");
        }

        // 내용 (200자 제한)
        if (post.getContent() != null) {
            String content = post.getContent();
            if (content.length() > 200) {
                content = content.substring(0, 200) + "...";
            }
            sb.append("내용: ").append(content);
        }

        return sb.toString().trim();
    }
}

