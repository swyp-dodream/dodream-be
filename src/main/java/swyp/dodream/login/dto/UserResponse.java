package swyp.dodream.login.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import swyp.dodream.login.domain.AuthProvider;
import swyp.dodream.login.domain.User;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String profileImageUrl;
    private AuthProvider provider;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImageUrl(),
                user.getProvider()
        );
    }
}

