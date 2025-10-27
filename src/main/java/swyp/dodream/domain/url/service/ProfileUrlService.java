package swyp.dodream.domain.url.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.url.domain.ProfileUrl;
import swyp.dodream.domain.url.dto.ProfileUrlCreateRequest;
import swyp.dodream.domain.url.dto.ProfileUrlResponse;
import swyp.dodream.domain.url.dto.ProfileUrlUpdateRequest;
import swyp.dodream.domain.url.repository.ProfileUrlRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileUrlService {
    
    private final ProfileUrlRepository profileUrlRepository;
    private final ProfileRepository profileRepository;
    private final SnowflakeIdService snowflakeIdService;
    
    // URL 추가
    @Transactional
    public ProfileUrlResponse addUrl(Long userId, ProfileUrlCreateRequest request) {
        Profile profile = findProfileByUserId(userId);
        
        // 같은 라벨의 URL이 이미 있는지 확인
        if (profileUrlRepository.existsByProfileIdAndLabel(profile.getId(), request.getLabel())) {
            throw ExceptionType.CONFLICT_DUPLICATE.of("이미 등록된 " + request.getLabel().getDisplayName() + " URL이 있습니다");
        }
        
        Long snowflakeId = snowflakeIdService.generateId();
        ProfileUrl profileUrl = new ProfileUrl(snowflakeId, profile, request.getLabel(), request.getUrl());
        profile.addProfileUrl(profileUrl);
        
        ProfileUrl savedUrl = profileUrlRepository.save(profileUrl);
        return ProfileUrlResponse.from(savedUrl);
    }
    
    // URL 목록 조회
    public List<ProfileUrlResponse> getUrls(Long userId) {
        Profile profile = findProfileByUserId(userId);
        
        return profileUrlRepository.findByProfileId(profile.getId())
                .stream()
                .map(ProfileUrlResponse::from)
                .collect(Collectors.toList());
    }
    
    // URL 수정
    @Transactional
    public ProfileUrlResponse updateUrl(Long userId, Long urlId, ProfileUrlUpdateRequest request) {
        Profile profile = findProfileByUserId(userId);
        
        ProfileUrl profileUrl = profileUrlRepository.findById(urlId)
                .orElseThrow(() -> ExceptionType.USER_NOT_FOUND.of("URL을 찾을 수 없습니다"));
        
        // URL이 해당 사용자의 프로필에 속하는지 확인
        if (!profileUrl.getProfile().getId().equals(profile.getId())) {
            throw ExceptionType.FORBIDDEN.of("권한이 없습니다");
        }
        
        profileUrl.updateProfileUrl(request.getLabel(), request.getUrl());
        ProfileUrl savedUrl = profileUrlRepository.save(profileUrl);
        
        return ProfileUrlResponse.from(savedUrl);
    }
    
    // URL 삭제
    @Transactional
    public void deleteUrl(Long userId, Long urlId) {
        Profile profile = findProfileByUserId(userId);
        
        ProfileUrl profileUrl = profileUrlRepository.findById(urlId)
                .orElseThrow(() -> ExceptionType.USER_NOT_FOUND.of("URL을 찾을 수 없습니다"));
        
        // URL이 해당 사용자의 프로필에 속하는지 확인
        if (!profileUrl.getProfile().getId().equals(profile.getId())) {
            throw ExceptionType.FORBIDDEN.of("권한이 없습니다");
        }
        
        profile.removeProfileUrl(profileUrl);
        profileUrlRepository.delete(profileUrl);
    }
    
    // 사용자 ID로 프로필 조회
    private Profile findProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> ExceptionType.USER_NOT_FOUND.of("프로필을 찾을 수 없습니다"));
    }
}