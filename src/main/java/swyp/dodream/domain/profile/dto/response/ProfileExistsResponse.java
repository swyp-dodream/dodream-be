package swyp.dodream.domain.profile.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileExistsResponse {

    @Schema(description = "프로필 보유 여부", example = "true")
    private boolean exists;
}
