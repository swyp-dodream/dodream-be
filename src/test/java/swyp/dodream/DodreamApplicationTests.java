package swyp.dodream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import swyp.dodream.domain.search.repository.PostDocumentRepository;

@SpringBootTest(
    // 테스트 환경에서 외부 서비스 자동 설정 제외
    properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.repositories.enabled=false",  // Redis Repository 비활성화
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration"
    }
)
@ActiveProfiles("test")  // 테스트용 프로파일 사용 (H2 DB)
class DodreamApplicationTests {

    // Elasticsearch Repository를 Mock으로 처리하여 테스트에서 제외
    @MockBean
    private PostDocumentRepository postDocumentRepository;

    // CI/CD 환경에서 실제 Redis 연결을 시도하지 않도록 Mock 객체로 대체하기
    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void contextLoads() {
    }

}
