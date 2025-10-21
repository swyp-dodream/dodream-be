package swyp.dodream.jwt.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_TTL = 7; // 7일

    public TokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Refresh Token 저장
    public void saveRefreshToken(Long userId, String name, String refreshToken) {
        String key = buildRedisKey(userId, name);
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_TTL, TimeUnit.DAYS);
    }

    // Refresh Token 조회
    public String getRefreshToken(Long userId, String name) {
        String key = buildRedisKey(userId, name);
        return redisTemplate.opsForValue().get(key);
    }

    // Refresh Token 삭제 (로그아웃)
    public void deleteRefreshToken(Long userId, String name) {
        String key = buildRedisKey(userId, name);
        redisTemplate.delete(key);
    }

    // Refresh Token 검증
    public boolean validateRefreshToken(Long userId, String name, String refreshToken) {
        String savedToken = getRefreshToken(userId, name);
        return savedToken != null && savedToken.equals(refreshToken);
    }

    // Redis 키 생성: refresh_token:{name}:{userId}
    private String buildRedisKey(Long userId, String name) {
        // 이름에서 공백을 언더스코어로 변경
        String sanitizedName = name.replaceAll("\\s+", "_");
        return REFRESH_TOKEN_PREFIX + sanitizedName + ":" + userId;
    }
}

