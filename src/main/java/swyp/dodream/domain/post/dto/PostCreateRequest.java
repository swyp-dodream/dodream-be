package swyp.dodream.domain.post.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class PostCreateRequest implements PostRequest {

    @NotNull(message = "프로젝트 유형은 필수입니다.")
    private ProjectType projectType; // PROJECT / STUDY

    @NotNull(message = "모집 상태는 필수입니다.")
    private PostStatus status;

    @NotNull(message = "활동 방식은 필수입니다.")
    private ActivityMode activityMode; // online/offline/hybrid

    @NotBlank(message = "예상 활동 기간은 필수입니다.")
    @Size(max = 100, message = "활동 기간은 최대 100자까지 입력 가능합니다.")
    private String durationText;

    @NotNull(message = "모집 마감일은 필수입니다.")
    private LocalDateTime deadlineAt;

    // 분야(관심사)는 STUDY일 경우 선택, PROJECT일 경우 필수
    private List<Long> categoryIds;

    @NotEmpty(message = "기술 스택은 최소 1개 이상 선택해야 합니다.")
    private List<Long> stackIds;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotEmpty(message = "모집 직군은 최소 1개 이상 선택해야 합니다.")
    private List<PostRoleDto> roles;
}
