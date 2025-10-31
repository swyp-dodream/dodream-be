package swyp.dodream.domain.profile.dto.response.master;

import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.master.domain.RoleCode;

public record ProfileRoleResponse(
        Long id,
        RoleCode code,
        String name
) {
    public static ProfileRoleResponse from(Role role) {
        return new ProfileRoleResponse(
                role.getId(),
                role.getCode(),
                role.getName()
        );
    }
}
