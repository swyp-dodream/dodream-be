package swyp.dodream.domain.url.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.url.enums.UrlLabel;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUrlCreateRequest {
    @NotNull(message = "URL 라벨은 필수입니다")
    private UrlLabel label;

    @NotBlank(message = "URL은 필수입니다")
    @Pattern(
            regexp = "^(https?://)?[\\w.-]+(\\.[\\w.-]+)*\\.[\\w.-]{2,}(/.*)?$",
            message = "유효한 URL 형식이 아닙니다"
    )
    private String url;
}