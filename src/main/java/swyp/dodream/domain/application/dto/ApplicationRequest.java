package swyp.dodream.domain.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApplicationRequest {

    @NotNull
    private String roleId; // 지원한 직군 ID

    @Size(max = 500)
    private String message; // 지원 메시지
}
