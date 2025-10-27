package swyp.dodream.domain.profile.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
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
    
    /**
     * 프로필 생성 (온보딩)
     */
    @Transactional
    public ProfileResponse createProfile(Long userId, ProfileCreateRequest request) {
        // 이미 프로필이 있는지 확인
        if (profileRepository.existsByUserId(userId)) {
            throw ExceptionType.CONFLICT_DUPLICATE.of("이미 프로필이 존재합니다");
        }
        
        // 닉네임 중복 확인
        if (profileRepository.existsByNickname(request.getNickname())) {
            throw ExceptionType.CONFLICT_DUPLICATE.of("이미 사용 중인 닉네임입니다");
        }
        
        Profile profile = new Profile(
                userId,
                request.getNickname(),
                request.getExperience(),
                request.getActivityMode()
        );
        
        // 전체 프로필 정보 설정
        profile.updateProfile(
                request.getNickname(),
                request.getGender(),
                request.getAgeBand(),
                request.getExperience(),
                request.getRoleId(),
                request.getActivityMode(),
                request.getIntroText(),
                request.getIntroIsAi(),
                request.getIsPublic()
        );
        
        Profile savedProfile = profileRepository.save(profile);
        return ProfileResponse.from(savedProfile);
    }
    
    /**
     * 프로필 조회
     */
    public ProfileResponse getProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> ExceptionType.USER_NOT_FOUND.of("프로필을 찾을 수 없습니다"));
        
        return ProfileResponse.from(profile);
    }
    
    /**
     * 프로필 수정
     */
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> ExceptionType.USER_NOT_FOUND.of("프로필을 찾을 수 없습니다"));
        
        // 닉네임 변경 시 중복 확인
        if (!profile.getNickname().equals(request.getNickname()) && 
            profileRepository.existsByNickname(request.getNickname())) {
            throw ExceptionType.CONFLICT_DUPLICATE.of("이미 사용 중인 닉네임입니다");
        }
        
        profile.updateProfile(
                request.getNickname(),
                request.getGender(),
                request.getAgeBand(),
                request.getExperience(),
                request.getRoleId(),
                request.getActivityMode(),
                request.getIntroText(),
                request.getIntroIsAi(),
                request.getIsPublic()
        );
        
        Profile savedProfile = profileRepository.save(profile);
        return ProfileResponse.from(savedProfile);
    }
    
    /**
     * 프로필 삭제
     */
    @Transactional
    public void deleteProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> ExceptionType.USER_NOT_FOUND.of("프로필을 찾을 수 없습니다"));
        
        profileRepository.delete(profile);
    }
}
