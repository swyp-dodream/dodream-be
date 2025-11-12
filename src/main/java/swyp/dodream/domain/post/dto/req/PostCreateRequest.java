package swyp.dodream.domain.post.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.post.common.*;
import swyp.dodream.domain.post.dto.PostRoleDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "모집글 생성 요청 DTO")
public class PostCreateRequest implements PostRequest {

    @NotNull(message = "프로젝트 유형은 필수입니다.")
    @Schema(description = "모집 유형", example = "PROJECT")
    private ProjectType projectType; // PROJECT / STUDY

    @NotNull(message = "모집 상태는 필수입니다.")
    @Schema(description = "모집 상태", example = "RECRUITING")
    private PostStatus status;

    @NotNull(message = "활동 방식은 필수입니다.")
    @Schema(description = "활동 방식", example = "ONLINE")
    private ActivityMode activityMode; // ONLINE / OFFLINE / HYBRID

    @NotNull(message = "활동 기간은 필수입니다.")
    @Schema(description = "활동 기간", example = "THREE_MONTHS")
    private DurationPeriod duration;

    @NotNull(message = "모집 마감일은 필수입니다.")
    @Schema(description = "모집 마감일", example = "2025-12-31T23:59:59")
    private LocalDateTime deadlineAt;

    @Schema(description = "관심 분야 ID 목록 (STUDY는 선택, PROJECT는 필수)")
    private List<Long> interestIds;

    @NotEmpty(message = "기술 스택은 최소 1개 이상 선택해야 합니다.")
    @Schema(description = "기술 스택 ID 목록", example = "[1, 2, 3]")
    private List<Long> stackIds;

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "모집글 제목", example = "Spring Boot 스터디 팀원 모집")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "모집글 내용", example = "3개월간 주 2회 스터디 진행 예정입니다.")
    private String content;

    @NotEmpty(message = "모집 직군은 최소 1개 이상 선택해야 합니다.")
    @Schema(description = "모집 직군 목록")
    private List<PostRoleDto> roles;
}
