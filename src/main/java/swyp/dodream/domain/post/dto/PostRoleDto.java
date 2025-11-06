package swyp.dodream.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostRoleDto {
    private Long roleId;  // 직군 id
    private int count;    // 인원 수
}