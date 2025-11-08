package swyp.dodream.domain.post.dto.response;


import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SuggestionResponse(
        Long id,
        Long postId,
        Long toUserId,
        Long fromUserId,
        String suggestionMessage,
        LocalDateTime createdAt
) {}