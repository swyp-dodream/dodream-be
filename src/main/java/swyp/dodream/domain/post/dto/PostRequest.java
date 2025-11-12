package swyp.dodream.domain.post.dto;

import swyp.dodream.domain.post.common.ActivityMode;
import swyp.dodream.domain.post.common.ProjectType;
import swyp.dodream.domain.post.common.PostStatus;
import swyp.dodream.domain.post.common.DurationPeriod;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRequest {

    ProjectType getProjectType();

    PostStatus getStatus();

    ActivityMode getActivityMode();

    DurationPeriod getDuration();

    LocalDateTime getDeadlineAt();

    List<Long> getInterestIds();

    List<Long> getStackIds();

    String getTitle();

    String getContent();

    List<PostRoleDto> getRoles();
}
