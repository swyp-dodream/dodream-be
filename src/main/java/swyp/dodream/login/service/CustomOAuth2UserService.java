package swyp.dodream.login.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.user.domain.OAuthAccount;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.OAuthAccountRepository;
import swyp.dodream.domain.user.repository.UserRepository;
import swyp.dodream.login.domain.AuthProvider;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final SnowflakeIdService snowflakeIdService;

    public CustomOAuth2UserService(UserRepository userRepository, OAuthAccountRepository oAuthAccountRepository, SnowflakeIdService snowflakeIdService) {
        this.userRepository = userRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.snowflakeIdService = snowflakeIdService;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        
        String email, name, picture, providerId;
        
        if (provider == AuthProvider.GOOGLE) {
            // Google OAuth2 사용자 정보 추출
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            picture = oAuth2User.getAttribute("picture");
            providerId = oAuth2User.getAttribute("sub");
        } else if (provider == AuthProvider.NAVER) {
            // Naver OAuth2 사용자 정보 추출 (네이버는 response 객체 안에 정보가 있음)
            java.util.Map<String, Object> naverResponse = oAuth2User.getAttribute("response");
            if (naverResponse != null) {
                email = (String) naverResponse.get("email");
                name = (String) naverResponse.get("name");
                picture = (String) naverResponse.get("profile_image");
                providerId = (String) naverResponse.get("id");
            } else {
                throw new OAuth2AuthenticationException("네이버 사용자 정보를 가져올 수 없습니다.");
            }
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth 제공자입니다: " + provider);
        }
        
        // OAuth 계정 조회 또는 생성
        OAuthAccount oAuthAccount = oAuthAccountRepository.findByEmailAndProvider(email, provider)
                .orElseGet(() -> {
                    // 새 사용자 생성 (스노우플레이크 ID 사용)
                    Long snowflakeId = snowflakeIdService.generateId();
                    User newUser = new User(snowflakeId, name, picture);
                    User savedUser = userRepository.save(newUser);
                    
                    // OAuth 계정 생성 (스노우플레이크 ID 사용)
                    Long oauthSnowflakeId = snowflakeIdService.generateId();
                    OAuthAccount newOAuthAccount = new OAuthAccount(oauthSnowflakeId, savedUser.getId(), provider, email);
                    return oAuthAccountRepository.save(newOAuthAccount);
                });
        
        // 사용자 정보 업데이트
        User user = userRepository.findById(oAuthAccount.getUserId()).orElseThrow();
        user.updateProfile(name, picture);
        userRepository.save(user);
        
        // 최근 로그인 시간 업데이트
        oAuthAccount.updateLastLoginAt();
        oAuthAccountRepository.save(oAuthAccount);
        
        return oAuth2User;
    }
}

