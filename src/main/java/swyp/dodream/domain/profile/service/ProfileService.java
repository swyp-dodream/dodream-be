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
import swyp.dodream.domain.profile.dto.request.AccountSettingsUpdateRequest;
import swyp.dodream.domain.profile.dto.request.ProfileCreateRequest;
import swyp.dodream.domain.profile.dto.request.ProfileMyPageUpdateRequest;
import swyp.dodream.domain.profile.dto.response.AccountSettingsResponse;
import swyp.dodream.domain.profile.dto.response.ProfileMyPageResponse;
import swyp.dodream.domain.profile.dto.response.ProfileResponse;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.proposal.domain.ProposalNotification;
import swyp.dodream.domain.proposal.repository.ProposalNotificationRepository;
import swyp.dodream.domain.url.domain.ProfileUrl;
import swyp.dodream.domain.url.enums.UrlLabel;
import swyp.dodream.domain.url.repository.ProfileUrlRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final int MAX_URLS = 3;

    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final InterestKeywordRepository interestKeywordRepository;
    private final TechSkillRepository techSkillRepository;
    private final ProposalNotificationRepository proposalNotificationRepository;
    private final SnowflakeIdService snowflakeIdService;
    private final ProfileUrlRepository profileUrlRepository;


    @Transactional
    public ProfileResponse createProfile(Long userId, ProfileCreateRequest request) {
        // 1) 단일 프로필 & 닉네임 중복
        if (profileRepository.existsByUserId(userId)) {
            throw new CustomException(ExceptionType.CONFLICT_DUPLICATE);
        }
        if (profileRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ExceptionType.CONFLICT_DUPLICATE);
        }

        // 2) 마스터 조회 + 개수 일치 검증
        List<Role> roles = roleRepository.findByNameIn(request.roleNames());
        requireSameCount(ExceptionType.NOT_FOUND, "직군", request.roleNames(), roles, Role::getName);

        List<InterestKeyword> interestKeywords = interestKeywordRepository.findByNameIn(request.interestKeywordNames());
        requireSameCount(ExceptionType.INTEREST_NOT_FOUND, "관심 키워드", request.interestKeywordNames(), interestKeywords, InterestKeyword::getName);

        List<TechSkill> techSkills = techSkillRepository.findByNameIn(request.techSkillNames());
        requireSameCount(ExceptionType.TECH_STACK_NOT_FOUND, "기술 스택", request.techSkillNames(), techSkills, TechSkill::getName);

        // 3) 프로필 생성 (공개 true 기본)
        Long profileId = snowflakeIdService.generateId();
        Profile profile = new Profile(
                profileId, userId,
                request.nickname(),
                request.experience(),
                request.activityMode()
        );
        profile.updateProfile(
                request.nickname(),
                request.gender(),
                request.ageBand(),
                request.experience(),
                request.activityMode(),
                request.introText(),
                true
        );

        roles.forEach(profile::addRole);
        interestKeywords.forEach(profile::addInterestKeyword);
        techSkills.forEach(profile::addTechSkill);

        // 4) URL (최대 3개, 중복 제거)
        if (request.profileUrls() != null && !request.profileUrls().isEmpty()) {
            if (request.profileUrls().size() > MAX_URLS) {
                throw new CustomException(ExceptionType.BAD_REQUEST_INVALID);
            }
            // label+url 기준 중복 제거
            Set<String> seen = new HashSet<>();
            for (Map.Entry<String, String> e : request.profileUrls().entrySet()) {
                UrlLabel label = resolveUrlLabel(e.getKey()); // 한/영/별칭 대응
                String url = e.getValue();

                String key = label.name() + "|" + url;
                if (!seen.add(key)) continue; // 중복 skip

                Long urlId = snowflakeIdService.generateId();
                ProfileUrl pu = new ProfileUrl(urlId, profile, label, url);
                profile.addProfileUrl(pu); // cascade = ALL, orphanRemoval = true 전제
            }
        }

        // 5) 저장
        Profile saved = profileRepository.save(profile);

        // 6) 제안 수신 설정
        Long pnId = snowflakeIdService.generateId();
        ProposalNotification pn = new ProposalNotification(
                pnId, saved.getId(),
                request.projectProposalEnabled(),
                request.studyProposalEnabled()
        );
        proposalNotificationRepository.save(pn);

        return ProfileResponse.from(saved, pn);
    }

    @Transactional(readOnly = true)
    public ProfileMyPageResponse getMyProfile(Long userId) {
        Profile profile = profileRepository.findWithAllByUserId(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND));
        return ProfileMyPageResponse.from(profile);
    }

    // ===== helpers =====

    private UrlLabel resolveUrlLabel(String raw) {
        if (raw == null) return UrlLabel.기타;
        String norm = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");

        Map<String, UrlLabel> alias = new HashMap<>();
        alias.put("github", UrlLabel.깃허브);   alias.put("깃허브", UrlLabel.깃허브); alias.put("git", UrlLabel.깃허브);
        alias.put("blog", UrlLabel.블로그);     alias.put("블로그", UrlLabel.블로그); alias.put("velog", UrlLabel.블로그); alias.put("티스토리", UrlLabel.블로그); alias.put("tistory", UrlLabel.블로그);
        alias.put("portfolio", UrlLabel.포트폴리오); alias.put("포트폴리오", UrlLabel.포트폴리오); alias.put("포폴", UrlLabel.포트폴리오);
        alias.put("notion", UrlLabel.노션);     alias.put("노션", UrlLabel.노션);
        UrlLabel mapped = alias.get(norm);
        if (mapped != null) return mapped;

        // enum 상수 그대로 들어온 경우 대비
        try { return UrlLabel.valueOf(raw.trim().toUpperCase(Locale.ROOT)); }
        catch (Exception ignore) {}
        return UrlLabel.기타;
    }

    private <T> void requireSameCount(
            ExceptionType type,
            String label,
            List<String> requested,
            List<T> loaded,
            Function<T, String> nameFn) {

        if (loaded.size() != requested.size()) {
            Set<String> found = loaded.stream()
                    .map(nameFn)
                    .collect(Collectors.toSet());

            List<String> missing = requested.stream()
                    .filter(n -> !found.contains(n))
                    .toList();

            throw type.of(label + " 누락: " + missing);
        }
    }

    @Transactional(readOnly = true)
    public AccountSettingsResponse getAccountSettingsWithEmail(Long userId, String email) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.PROFILE_NOT_FOUND));
        ProposalNotification pn = proposalNotificationRepository.findByProfileId(profile.getId())
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND));

        return AccountSettingsResponse.builder()
                .email(email)
                .gender(profile.getGender())
                .ageBand(profile.getAgeBand())
                .proposalProjectOn(pn.getProposalProjectOn())
                .proposalStudyOn(pn.getProposalStudyOn())
                .isPublic(profile.getIsPublic())
                .build();
    }

    @Transactional
    public ProfileMyPageResponse updateMyProfile(Long userId, ProfileMyPageUpdateRequest req) {
        Profile profile = profileRepository.findWithAllByUserId(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.PROFILE_NOT_FOUND));

        // 닉네임 중복 체크
        if (!req.getNickname().equals(profile.getNickname())
                && profileRepository.existsByNickname(req.getNickname())) {
            throw new CustomException(ExceptionType.CONFLICT_DUPLICATE, "이미 사용 중인 닉네임입니다.");
        }

        profile.updateProfile(
                req.getNickname(),
                profile.getGender(),     // 계정설정 API에서 관리
                profile.getAgeBand(),
                req.getExperience(),
                req.getActivityMode(),
                req.getIntroText(),
                profile.getIsPublic()
        );

        // 직군
        if (req.getRoleNames() != null && !req.getRoleNames().isEmpty()) {
            var roles = roleRepository.findByNameIn(req.getRoleNames());
            requireSameCount(ExceptionType.NOT_FOUND, "직군", req.getRoleNames(), roles, Role::getName);
            profile.clearRoles();
            roles.forEach(profile::addRole);
        }

        // 관심
        if (req.getInterestKeywordNames() != null && !req.getInterestKeywordNames().isEmpty()) {
            if (req.getInterestKeywordNames().size() > 5)
                throw new CustomException(ExceptionType.BAD_REQUEST_INVALID, "관심 분야는 최대 5개까지 선택 가능합니다.");
            var interests = interestKeywordRepository.findByNameIn(req.getInterestKeywordNames());
            requireSameCount(ExceptionType.INTEREST_NOT_FOUND, "관심 키워드", req.getInterestKeywordNames(), interests, InterestKeyword::getName);
            profile.clearInterestKeywords();
            interests.forEach(profile::addInterestKeyword);
        }

        // 기술
        if (req.getTechSkillNames() != null && !req.getTechSkillNames().isEmpty()) {
            if (req.getTechSkillNames().size() > 5)
                throw new CustomException(ExceptionType.BAD_REQUEST_INVALID, "기술 스택은 최대 5개까지 선택 가능합니다.");
            var skills = techSkillRepository.findByNameIn(req.getTechSkillNames());
            requireSameCount(ExceptionType.TECH_STACK_NOT_FOUND, "기술 스택", req.getTechSkillNames(), skills, TechSkill::getName);
            profile.clearTechSkills();
            skills.forEach(profile::addTechSkill);
        }

        // URL (최대 3개) — orphanRemoval=true면 clear()만으로 삭제됨
        if (req.getProfileUrls() != null && !req.getProfileUrls().isEmpty()) {
            if (req.getProfileUrls().size() > MAX_URLS)
                throw new CustomException(ExceptionType.BAD_REQUEST_INVALID, "프로필 URL은 최대 3개까지 등록 가능합니다.");

            profile.getProfileUrls().clear();

            Set<String> dedup = new HashSet<>();
            req.getProfileUrls().forEach((labelStr, url) -> {
                var label = resolveUrlLabel(labelStr);
                var key = label.name() + "|" + url;
                if (!dedup.add(key)) return;     // 중복 제거

                Long urlId = snowflakeIdService.generateId();
                profile.addProfileUrl(new ProfileUrl(urlId, profile, label, url));
            });
        }

        Profile saved = profileRepository.save(profile);
        return ProfileMyPageResponse.from(saved);
    }

    @Transactional
    public AccountSettingsResponse updateAccountSettings(Long userId, String email, AccountSettingsUpdateRequest req) {
        // 1) 프로필 조회
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.PROFILE_NOT_FOUND));

        // 2) 프로필(계정 설정) 갱신
        profile.updateAccountSettings(req.getGender(), req.getAgeBand(), req.getIsPublic());

        // 3) 제안 수신 설정 upsert
        ProposalNotification pn = proposalNotificationRepository.findByProfileId(profile.getId())
                .map(existing -> {
                    existing.setProposalProjectOn(req.getProposalProjectOn());
                    existing.setProposalStudyOn(req.getProposalStudyOn());
                    return existing;
                })
                .orElseGet(() -> new ProposalNotification(
                        snowflakeIdService.generateId(),
                        profile.getId(),
                        req.getProposalProjectOn(),
                        req.getProposalStudyOn()
                ));

        proposalNotificationRepository.save(pn);
        profileRepository.save(profile);

        // 4) 응답 조립
        return AccountSettingsResponse.builder()
                .email(email)
                .gender(profile.getGender())
                .ageBand(profile.getAgeBand())
                .proposalProjectOn(pn.getProposalProjectOn())
                .proposalStudyOn(pn.getProposalStudyOn())
                .isPublic(profile.getIsPublic())
                .build();
    }
}