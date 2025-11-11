package swyp.dodream.domain.profile.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 중복 체크 응답 DTO")
public record NicknameCheckResponse(

        @Schema(description = "닉네임 사용 가능 여부", example = "true")
        boolean available,

        @Schema(description = "검사한 닉네임", example = "현우")
        String nickname
) { }