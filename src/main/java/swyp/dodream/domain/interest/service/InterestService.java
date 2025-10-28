package swyp.dodream.domain.interest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.interest.domain.Interest;
import swyp.dodream.domain.interest.dto.ProfileInterestResponse;
import swyp.dodream.domain.interest.enums.InterestEnum;
import swyp.dodream.domain.interest.repository.InterestRepository;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

    private final InterestRepository interestRepository;
    private final ProfileRepository profileRepository;
    private final SnowflakeIdService snowflakeIdService;

    /**
     * 모든 관심 분야 조회 (마스터 데이터)
     */
    public List<ProfileInterestResponse> getAllInterests() {
        return Arrays.stream(InterestEnum.values())
                .map(ProfileInterestResponse::ofEnum)
                .toList();
    }

    /**
     * 사용자 프로필의 관심 분야 조회
     */
    public List<ProfileInterestResponse> getUserInterests(Long profileId) {
        List<Interest> interests = interestRepository.findByProfileId(profileId);
        return interests.stream()
                .map(ProfileInterestResponse::of)
                .toList();
    }

    /**
     * 관심 분야 추가
     */
    @Transactional
    public ProfileInterestResponse addInterest(Long profileId, InterestEnum interest) {
        Profile profile = findProfileById(profileId);

        // 이미 존재하는지 확인
        if (interestRepository.existsByProfileIdAndInterest(profileId, interest)) {
            throw ExceptionType.CONFLICT_DUPLICATE.of("이미 추가된 관심 분야입니다");
        }

        Long interestId = snowflakeIdService.generateId();
        Interest newInterest = new Interest(interestId, profile, interest);
        Interest savedInterest = interestRepository.save(newInterest);

        return ProfileInterestResponse.of(savedInterest);
    }

    /**
     * 관심 분야 삭제
     */
    @Transactional
    public void removeInterest(Long profileId, InterestEnum interest) {
        Profile profile = findProfileById(profileId);

        // 존재하는지 확인
        if (!interestRepository.existsByProfileIdAndInterest(profileId, interest)) {
            throw ExceptionType.NOT_FOUND.of("관심 분야를 찾을 수 없습니다");
        }

        interestRepository.deleteByProfileAndInterest(profile, interest);
    }

    // === Private Helper Methods ===

    private Profile findProfileById(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> ExceptionType.NOT_FOUND.of("프로필을 찾을 수 없습니다"));
    }
}
