package swyp.dodream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // 테스트용 프로파일 사용 (H2 DB)
class DodreamApplicationTests {

    @Test
    void contextLoads() {
    }

}
