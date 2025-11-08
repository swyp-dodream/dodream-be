package swyp.dodream.domain.post.dto.res;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record RecruitApplicationDetailResponse(
        Long applicationId,
        Long userId,
        String nickname,
        String profileImage,
        String status,              // APPLIED 등
        LocalDateTime createdAt,    // 지원 시간
        String experience,          // 프로필 경력
        List<String> jobGroups,     // 프로필에 등록된 직군 목록

        Long appliedRoleId,         // 이 모집글에 "이 역할로" 지원했다
        String appliedRoleName,     // 역할 이름 (백엔드, iOS ...)
        String message              // 지원 시 작성한 메시지
) {
}