package swyp.dodream.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import swyp.dodream.domain.user.domain.OAuthAccount;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.login.domain.AuthProvider;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String profileImageUrl;
    private AuthProvider provider;
    private LocalDateTime lastLoginAt;

    public static UserResponse from(User user, OAuthAccount oAuthAccount) {
        return new UserResponse(
                user.getId(),
                oAuthAccount.getEmail(),
                user.getName(),
                user.getProfileImageUrl(),
                oAuthAccount.getProvider(),
                oAuthAccount.getLastLoginAt()
        );
    }
}

