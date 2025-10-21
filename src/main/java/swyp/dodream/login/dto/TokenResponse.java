package swyp.dodream.login.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String message;

    public static TokenResponse of(String accessToken) {
        return new TokenResponse(accessToken, "토큰 재발급 성공");
    }
}

