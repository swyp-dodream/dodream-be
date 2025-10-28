package swyp.dodream.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.ProjectType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class PostCreateRequest {
    @NotNull
    private ProjectType projectType; // "project" or "study"

    @NotNull
    private ActivityMode activityMode; // "online" / "offline" / "hybrid"

    @Size(max = 100)
    private String durationText; // 예상 활동 기간

    private LocalDateTime deadlineAt; // 모집 마감일

    private List<Long> categoryIds;   // 분야 id 리스트

    private List<Long> stackIds;      // 기술 스택 id 리스트

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private List<PostRoleDto> roles;  // 모집 직군별 인원
}
