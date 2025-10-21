package swyp.dodream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    // 테스트 환경에서 Redis 자동 설정 제외 (임베디드 Redis 없이 테스트)
    properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
    }
)
@ActiveProfiles("test")  // 테스트용 프로파일 사용 (H2 DB)
class DodreamApplicationTests {

    @Test
    void contextLoads() {
    }

}
