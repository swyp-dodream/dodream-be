package swyp.dodream.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class UserPrincipal {
    private final Long userId;
    private final String email;
    private final String name;
}