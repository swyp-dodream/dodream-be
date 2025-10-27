package swyp.dodream.login.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.user.domain.OAuthAccount;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.dto.UserResponse;
import swyp.dodream.domain.user.repository.OAuthAccountRepository;
import swyp.dodream.domain.user.repository.UserRepository;
import swyp.dodream.jwt.service.TokenService;
import swyp.dodream.jwt.util.JwtUtil;
import swyp.dodream.login.dto.TokenResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;

    // 토큰 재발급
    public TokenResponse reissueToken(String refreshToken) {
        // Refresh Token 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw ExceptionType.UNAUTHORIZED_TOKEN_INVALID.of();
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ExceptionType.NOT_FOUND_USER.of());
        
        // OAuth 계정 조회 (이메일 정보를 위해)
        OAuthAccount oAuthAccount = oAuthAccountRepository.findByUserId(userId)
                .orElseThrow(() -> ExceptionType.NOT_FOUND_USER.of());
        
        // Redis에 저장된 Refresh Token과 비교 (이름 포함)
        if (!tokenService.validateRefreshToken(userId, user.getName(), refreshToken)) {
            throw ExceptionType.UNAUTHORIZED_REFRESH_TOKEN_INVALID.of("만료되었거나 유효하지 않은 토큰입니다");
        }

        // 새로운 Access Token 발급 (이름 포함)
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), oAuthAccount.getEmail(), user.getName());

        return TokenResponse.of(newAccessToken);
    }

    // 로그아웃
    @Transactional
    public void logout(Long userId, String name) {
        // Redis에서 Refresh Token 삭제 (이름 포함)
        tokenService.deleteRefreshToken(userId, name);
    }

    // 현재 사용자 정보 조회
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ExceptionType.NOT_FOUND_USER.of());
        
        OAuthAccount oAuthAccount = oAuthAccountRepository.findByUserId(userId)
                .orElseThrow(() -> ExceptionType.NOT_FOUND_USER.of());

        return UserResponse.from(user, oAuthAccount);
    }
}

