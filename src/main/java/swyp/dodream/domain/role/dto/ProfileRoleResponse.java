package swyp.dodream.domain.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import swyp.dodream.domain.role.domain.Role;
import swyp.dodream.domain.role.enums.RoleEnum;

import java.time.LocalDateTime;

public record ProfileRoleResponse(
        @Schema(description = "프로필 직군 ID")
        Long id,

        @Schema(description = "직군 코드", example = "FE")
        String roleCode,

        @Schema(description = "직군 이름", example = "프론트엔드")
        String roleName,

        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {
    public static ProfileRoleResponse of(Role role) {
        RoleEnum roleEnum = role.getRole();
        return new ProfileRoleResponse(
                role.getId(),
                roleEnum.getCode(),
                roleEnum.getName(),
                role.getCreatedAt()
        );
    }

    public static ProfileRoleResponse ofEnum(RoleEnum role) {
        return new ProfileRoleResponse(
                null,
                role.getCode(),
                role.getName(),
                null
        );
    }
}
