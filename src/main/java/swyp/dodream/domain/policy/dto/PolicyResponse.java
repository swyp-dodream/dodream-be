package swyp.dodream.domain.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PolicyResponse {
    private String title;
    private String content; // Markdown 원문 그대로 반환
}
