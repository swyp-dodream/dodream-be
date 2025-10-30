package swyp.dodream.domain.url.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.url.domain.ProfileUrl;
import swyp.dodream.domain.url.enums.UrlLabel;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUrlResponse {
    private Long id;
    private Long profileId;
    private UrlLabel label;
    private String url;
    private String createdAt;
    private String updatedAt;

    public static ProfileUrlResponse from(ProfileUrl profileUrl) {
        return new ProfileUrlResponse(
                profileUrl.getId(),
                profileUrl.getProfile().getId(),
                profileUrl.getLabel(),
                profileUrl.getUrl(),
                profileUrl.getCreatedAt() != null ? profileUrl.getCreatedAt().toString() : null,
                profileUrl.getUpdatedAt() != null ? profileUrl.getUpdatedAt().toString() : null
        );
    }
}
