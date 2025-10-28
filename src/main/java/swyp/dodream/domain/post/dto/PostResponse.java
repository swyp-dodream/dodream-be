package swyp.dodream.domain.post.dto;

import lombok.Builder;
import lombok.Getter;
import swyp.dodream.domain.Post;
import swyp.dodream.domain.post.common.PostStatus;

@Getter
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private PostStatus status;
    private String activityMode;
    private boolean isOwner;

    public static PostResponse from(Post post, boolean isOwner) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .activityMode(post.getActivityMode().name())
                .isOwner(isOwner)
                .build();
    }
}
