package swyp.dodream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import swyp.dodream.domain.search.repository.PostDocumentRepository;

@SpringBootTest(
    // 테스트 환경에서 외부 서비스 자동 설정 제외
    properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration"
    }
)
@ActiveProfiles("test")  // 테스트용 프로파일 사용 (H2 DB)
class DodreamApplicationTests {

    // Elasticsearch Repository를 Mock으로 처리하여 테스트에서 제외
    @MockBean
    private PostDocumentRepository postDocumentRepository;

    @Test
    void contextLoads() {
    }

}
