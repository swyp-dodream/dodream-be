package swyp.dodream.domain.profile.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.profile.enums.ActivityMode;
import swyp.dodream.domain.profile.enums.Experience;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileMyPageUpdateRequest {

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 10, message = "닉네임은 10자 이내여야 합니다.")
    @Pattern(
            regexp = "^[가-힣a-zA-Z0-9]{1,10}$",
            message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다."
    )
    private String nickname;

    @NotNull(message = "경력 단계를 선택해야 합니다.")
    private Experience experience;

    @NotNull(message = "활동 방식을 선택해야 합니다.")
    private ActivityMode activityMode;

    @Size(max = 200, message = "자기소개는 200자 이내로 입력해주세요.")
    private String introText;

    @NotNull(message = "직군은 필수입니다.")
    @Size(min = 1, max = 3, message = "직군은 1개 이상 3개 이하여야 합니다.")
    private List<String> roleNames;

    @NotNull(message = "기술 스택은 필수입니다.")
    @Size(min = 1, max = 5, message = "기술 스택은 1개 이상 5개 이하여야 합니다.")
    private List<String> techSkillNames;

    @NotNull(message = "관심 분야는 필수입니다.")
    @Size(min = 1, max = 5, message = "관심 분야는 1개 이상 5개 이하여야 합니다.")
    private List<String> interestKeywordNames;
}
