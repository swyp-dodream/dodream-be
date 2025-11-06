package swyp.dodream.domain.post.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostRoleReqeust {
    private Long roleId;  // 직군 id
    private int count;    // 인원 수
}