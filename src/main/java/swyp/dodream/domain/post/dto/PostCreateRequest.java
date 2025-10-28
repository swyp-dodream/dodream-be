package swyp.dodream.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.ProjectType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostCreateRequest {
    @NotNull
    private ProjectType projectType;
    @NotNull private ActivityMode activityMode;
    @Size(max = 100)
    private String durationText;
    private LocalDateTime deadlineAt;
    @NotBlank
    private String title;
    @NotBlank private String content;
}
