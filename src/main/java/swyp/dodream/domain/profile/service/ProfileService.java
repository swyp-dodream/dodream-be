package swyp.dodream.domain.profile.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.master.domain.InterestKeyword;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.master.domain.TechSkill;
import swyp.dodream.domain.master.repository.InterestKeywordRepository;
import swyp.dodream.domain.master.repository.RoleRepository;
import swyp.dodream.domain.master.repository.TechSkillRepository;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.dto.request.ProfileCreateRequest;
import swyp.dodream.domain.profile.dto.response.ProfileResponse;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.proposal.domain.ProposalNotification;
import swyp.dodream.domain.proposal.repository.ProposalNotificationRepository;
import swyp.dodream.domain.url.domain.ProfileUrl;
import swyp.dodream.domain.url.enums.UrlLabel;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final InterestKeywordRepository interestKeywordRepository;
    private final TechSkillRepository techSkillRepository;
    private final ProposalNotificationRepository proposalNotificationRepository;
    private final SnowflakeIdService snowflakeIdService;

    /**
     * 프로필 생성
     * 온보딩을 통해 새로운 사용자의 프로필을 생성합니다.
     * 기본값으로 공개(isPublic = true), 지원 제안 수신(proposal ON) 설정됩니다.
     */
    @Transactional
    public ProfileResponse createProfile(Long userId, ProfileCreateRequest request) {
        // 1. 프로필 중복 확인
        if (profileRepository.existsByUserId(userId)) {
            throw new CustomException(ExceptionType.CONFLICT_DUPLICATE);
        }

        // 2. 닉네임 중복 확인
        if (profileRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ExceptionType.CONFLICT_DUPLICATE);
        }

        // 3. 직군 조회
        List<Role> roles = roleRepository.findByNameIn(request.roleNames());
        if (roles.size() != request.roleNames().size()) {
            throw new CustomException(ExceptionType.NOT_FOUND);
        }

        // 4. 관심 분야 조회
        List<InterestKeyword> interestKeywords = interestKeywordRepository.findByNameIn(request.interestKeywordNames());
        if (interestKeywords.size() != request.interestKeywordNames().size()) {
            throw new CustomException(ExceptionType.INTEREST_NOT_FOUND);
        }

        // 5. 기술 스택 조회
        List<TechSkill> techSkills = techSkillRepository.findByNameIn(request.techSkillNames());
        if (techSkills.size() != request.techSkillNames().size()) {
            throw new CustomException(ExceptionType.TECH_STACK_NOT_FOUND);
        }

        // 6. 프로필 생성 (Snowflake ID 사용)
        Long profileId = snowflakeIdService.generateId();
        Profile profile = new Profile(
                profileId,
                userId,
                request.nickname(),
                request.experience(),
                request.activityMode()
        );

        // 기본 정보 설정
        profile.updateProfile(
                request.nickname(),
                request.gender(),
                request.ageBand(),
                request.experience(),
                request.activityMode(),
                request.introText(),
                true // isPublic = true (기본값)
        );

        // 7. 직군 추가
        roles.forEach(profile::addRole);

        // 8. 관심 분야 추가
        interestKeywords.forEach(profile::addInterestKeyword);

        // 9. 기술 스택 추가
        techSkills.forEach(profile::addTechSkill);

        // 프로필 먼저 저장 (ProfileUrl이 profile을 참조하므로 먼저 저장 필요)
        Profile savedProfile = profileRepository.save(profile);

        // 10. 프로필 URL 추가 (옵션) - 프로필 저장 후에 추가
        if (request.profileUrls() != null && !request.profileUrls().isEmpty()) {
            for (Map.Entry<String, String> entry : request.profileUrls().entrySet()) {
                try {
                    UrlLabel label = UrlLabel.valueOf(entry.getKey());
                    Long urlId = snowflakeIdService.generateId();
                    ProfileUrl profileUrl = new ProfileUrl(urlId, savedProfile, label, entry.getValue());
                    savedProfile.addProfileUrl(profileUrl);
                } catch (IllegalArgumentException e) {
                    throw new CustomException(ExceptionType.BAD_REQUEST_INVALID);
                }
            }
            // ProfileUrl 추가 후 프로필 다시 저장
            savedProfile = profileRepository.save(savedProfile);
        }

        // 11. ProposalNotification 생성 (지원 제안 수신 설정)
        Long notificationId = snowflakeIdService.generateId();
        ProposalNotification proposalNotification = new ProposalNotification(
                notificationId,
                savedProfile.getId(),
                request.projectProposalEnabled(),
                request.studyProposalEnabled()
        );
        proposalNotificationRepository.save(proposalNotification);

        // 응답 DTO 반환
        return ProfileResponse.from(savedProfile, proposalNotification);
    }
}