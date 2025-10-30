package swyp.dodream.domain.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import swyp.dodream.domain.profile.enums.*;

import java.util.List;
import java.util.Map;

/**
 * 프로필 생성 요청 DTO
 * 온보딩 시 프로필을 생성할 때 사용됩니다.
 */
@Schema(description = "프로필 생성 요청")
public record ProfileCreateRequest(
        @Schema(description = "닉네임 (1~10자)", example = "홍길동")
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하여야 합니다")
        String nickname,

        @Schema(description = "성별", example = "남성")
        @NotNull(message = "성별은 필수입니다")
        Gender gender,

        @Schema(description = "연령대", example = "이십대")
        @NotNull(message = "연령대는 필수입니다")
        AgeBand ageBand,

        @Schema(description = "경력", example = "신입")
        @NotNull(message = "경력은 필수입니다")
        Experience experience,

        @Schema(description = "선호 활동 방식", example = "온라인")
        @NotNull(message = "선호 활동 방식은 필수입니다")
        ActivityMode activityMode,

        @Schema(description = "직군 이름 목록 (1~3개)", example = "[\"백엔드\", \"iOS\"]")
        @NotNull(message = "직군은 필수입니다")
        @Size(min = 1, max = 3, message = "직군은 1개 이상 3개 이하여야 합니다")
        List<String> roleNames,

        @Schema(description = "관심 분야 이름 목록 (1~5개)", example = "[\"AI\", \"모빌리티\"]")
        @NotNull(message = "관심 분야는 필수입니다")
        @Size(min = 1, max = 5, message = "관심 분야는 1개 이상 5개 이하여야 합니다")
        List<String> interestKeywordNames,

        @Schema(description = "기술 스택 이름 목록 (1~5개)", example = "[\"Spring\", \"MySQL\", \"Java\"]")
        @NotNull(message = "기술 스택은 필수입니다")
        @Size(min = 1, max = 5, message = "기술 스택은 1개 이상 5개 이하여야 합니다")
        List<String> techSkillNames,

        @Schema(description = "자기소개 (최대 200자)", example = "안녕하세요. 백엔드 개발자입니다.")
        @Size(max = 200, message = "자기소개는 200자 이하여야 합니다")
        String introText,

        @Schema(description = "프로젝트 제안 수신 여부", example = "true")
        @NotNull(message = "프로젝트 제안 수신 여부는 필수입니다")
        Boolean projectProposalEnabled,

        @Schema(description = "스터디 제안 수신 여부", example = "true")
        @NotNull(message = "스터디 제안 수신 여부는 필수입니다")
        Boolean studyProposalEnabled,

        @Schema(description = "프로필 URL 목록 (최대 3개)", example = "{\"깃허브\": \"https://github.com/user\", \"포트폴리오\": \"https://portfolio.com\"}")
        @Size(max = 3, message = "프로필 URL은 최대 3개까지만 추가 가능합니다")
        Map<String, String> profileUrls
) {
}
