package swyp.dodream.domain.post.dto;

import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.common.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRequest {
    ProjectType getProjectType();
    PostStatus getStatus();
    ActivityMode getActivityMode();
    String getDurationText();
    LocalDateTime getDeadlineAt();
    List<Long> getCategoryIds();
    List<Long> getStackIds();
    String getTitle();
    String getContent();
    List<PostRoleDto> getRoles();
}
