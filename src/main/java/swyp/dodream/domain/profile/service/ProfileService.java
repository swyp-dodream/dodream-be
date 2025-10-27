package swyp.dodream.domain.profile.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.dto.ProfileCreateRequest;
import swyp.dodream.domain.profile.dto.ProfileResponse;
import swyp.dodream.domain.profile.dto.ProfileUpdateRequest;
import swyp.dodream.domain.profile.repository.ProfileRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {
    
    private final ProfileRepository profileRepository;
    private final SnowflakeIdService snowflakeIdService;
    
    // 프로필 생성 (온보딩)
    @Transactional
    public ProfileResponse createProfile(Long userId, ProfileCreateRequest request) {
        validateProfileCreation(userId, request);
        
        Profile profile = createNewProfile(userId, request);
        return ProfileResponse.from(profileRepository.save(profile));
    }
    
    // 프로필 조회
    public ProfileResponse getProfile(Long userId) {
        Profile profile = findProfileByUserId(userId);
        return ProfileResponse.from(profile);
    }
    
    // 프로필 수정
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        Profile profile = findProfileByUserId(userId);
        validateNicknameChange(profile.getNickname(), request.getNickname());
        
        profile.updateProfile(
                request.getNickname(),
                request.getGender(),
                request.getAgeBand(),
                request.getExperience(),
                request.getActivityMode(),
                request.getIntroText(),
                request.getIntroIsAi(),
                request.getIsPublic()
        );
        
        return ProfileResponse.from(profileRepository.save(profile));
    }
    
    // 프로필 삭제
    @Transactional
    public void deleteProfile(Long userId) {
        Profile profile = findProfileByUserId(userId);
        profileRepository.delete(profile);
    }
    
    // === Private Helper Methods ===
    
    // 사용자 ID로 프로필 조회
    private Profile findProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> ExceptionType.USER_NOT_FOUND.of("프로필을 찾을 수 없습니다"));
    }
    
    // 프로필 생성 시 유효성 검증
    private void validateProfileCreation(Long userId, ProfileCreateRequest request) {
        if (profileRepository.existsByUserId(userId)) {
            throw ExceptionType.CONFLICT_DUPLICATE.of("이미 프로필이 존재합니다");
        }
        
        if (profileRepository.existsByNickname(request.getNickname())) {
            throw ExceptionType.CONFLICT_DUPLICATE.of("이미 사용 중인 닉네임입니다");
        }
    }
    
    // 닉네임 변경 시 유효성 검증
    private void validateNicknameChange(String currentNickname, String newNickname) {
        if (!currentNickname.equals(newNickname) && 
            profileRepository.existsByNickname(newNickname)) {
            throw ExceptionType.CONFLICT_DUPLICATE.of("이미 사용 중인 닉네임입니다");
        }
    }
    
    // 새 프로필 생성
    private Profile createNewProfile(Long userId, ProfileCreateRequest request) {
        Long snowflakeId = snowflakeIdService.generateId();
        Profile profile = new Profile(
                snowflakeId,
                userId,
                request.getNickname(),
                request.getExperience(),
                request.getActivityMode()
        );
        
        profile.updateProfile(
                request.getNickname(),
                request.getGender(),
                request.getAgeBand(),
                request.getExperience(),
                request.getActivityMode(),
                request.getIntroText(),
                request.getIntroIsAi(),
                request.getIsPublic()
        );
        
        return profile;
    }
}
