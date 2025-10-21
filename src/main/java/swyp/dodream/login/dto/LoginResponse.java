package swyp.dodream.login.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String email;
    private String name;

    public static LoginResponse of(String accessToken, String refreshToken, Long userId, String email, String name) {
        return new LoginResponse(accessToken, refreshToken, userId, email, name);
    }
}

