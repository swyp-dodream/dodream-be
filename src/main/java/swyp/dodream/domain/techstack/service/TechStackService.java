package swyp.dodream.domain.techstack.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.techstack.domain.TechStack;
import swyp.dodream.domain.techstack.dto.ProfileTechStackResponse;
import swyp.dodream.domain.techstack.enums.TechStackEnum;
import swyp.dodream.domain.techstack.repository.TechStackRepository;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechStackService {

    private final TechStackRepository techStackRepository;
    private final ProfileRepository profileRepository;
    private final SnowflakeIdService snowflakeIdService;

    /**
     * 모든 기술 스택 조회 (마스터 데이터)
     */
    public List<ProfileTechStackResponse> getAllTechStacks() {
        return Arrays.stream(TechStackEnum.values())
                .map(ProfileTechStackResponse::ofEnum)
                .toList();
    }

    /**
     * 사용자 프로필의 기술 스택 조회
     */
    public List<ProfileTechStackResponse> getUserTechStacks(Long profileId) {
        List<TechStack> techStacks = techStackRepository.findByProfileId(profileId);
        return techStacks.stream()
                .map(ProfileTechStackResponse::of)
                .toList();
    }

    /**
     * 기술 스택 추가
     */
    @Transactional
    public ProfileTechStackResponse addTechStack(Long profileId, TechStackEnum techStack) {
        Profile profile = findProfileById(profileId);

        // 이미 존재하는지 확인
        if (techStackRepository.existsByProfileIdAndTechStack(profileId, techStack)) {
            throw ExceptionType.CONFLICT_DUPLICATE.of("이미 추가된 기술 스택입니다");
        }

        Long techStackId = snowflakeIdService.generateId();
        TechStack newTechStack = new TechStack(techStackId, profile, techStack);
        TechStack savedTechStack = techStackRepository.save(newTechStack);

        return ProfileTechStackResponse.of(savedTechStack);
    }

    /**
     * 기술 스택 삭제
     */
    @Transactional
    public void removeTechStack(Long profileId, TechStackEnum techStack) {
        Profile profile = findProfileById(profileId);

        // 존재하는지 확인
        if (!techStackRepository.existsByProfileIdAndTechStack(profileId, techStack)) {
            throw ExceptionType.NOT_FOUND.of("기술 스택을 찾을 수 없습니다");
        }

        techStackRepository.deleteByProfileAndTechStack(profile, techStack);
    }

    // === Private Helper Methods ===

    private Profile findProfileById(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> ExceptionType.NOT_FOUND.of("프로필을 찾을 수 없습니다"));
    }
}
