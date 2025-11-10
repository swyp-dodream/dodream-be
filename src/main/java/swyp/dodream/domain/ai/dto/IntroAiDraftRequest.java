package swyp.dodream.domain.ai.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import swyp.dodream.domain.profile.enums.AgeBand;
import swyp.dodream.domain.profile.enums.Experience;
import swyp.dodream.domain.profile.enums.ActivityMode;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntroAiDraftRequest {

    @NotBlank
    @Size(max = 10)
    private String nickname;

    @NotNull
    private AgeBand ageBand;

    @NotNull
    private Experience experience;

    @NotNull
    private ActivityMode activityMode;

    @Size(max = 500)
    private String introText;

    @Size(max = 3)
    List<String> profileUrls;

    @NotNull @Size(min = 1, max = 3)
    private List<String> roles;

    @NotNull @Size(min = 1, max = 5)
    private List<String> interestKeywords;

    @NotNull @Size(min = 1, max = 5)
    private List<String> techSkills;
}