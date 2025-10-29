package swyp.dodream.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.ProjectType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class PostUpdateRequest implements PostRequest {
    private ProjectType projectType;
    private PostStatus status;
    private ActivityMode activityMode;
    private String durationText;
    private LocalDateTime deadlineAt;
    private List<Long> categoryIds;
    private List<Long> stackIds;
    private String title;
    private String content;
    private List<PostRoleDto> roles;
}