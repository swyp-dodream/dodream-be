package swyp.dodream.domain.post.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RecruitApplicationDetailResponse(
        Long applicationId,
        Long userId,
        String nickname,
        String profileImage,
        String status,              // APPLIED 등
        LocalDateTime createdAt,    // 지원 시간
        String experience,          // 프로필 경력
        Long appliedRoleId,   // 모집글에 지원한 직군
        String appliedRoleName,  // 지원한 역할 이름 (백엔드, iOS ...)
        String message     // 지원 시 작성한 메시지
) {
}