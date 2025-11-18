package swyp.dodream.domain.home.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import swyp.dodream.domain.post.dto.response.PostSummaryResponse;

@Getter
@Builder
public class HomeResponse {
    private String profileImageUrl;
    private Page<PostSummaryResponse> posts;
}