package swyp.dodream.domain.search.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;
import swyp.dodream.domain.search.document.PostDocument;

/**
 * Elasticsearch 인덱스 초기화
 * 애플리케이션 시작 시 인덱스를 재생성합니다.
 *
 * 주의: 운영 환경에서는 사용하지 마세요! (데이터 손실)
 * @Profile("dev") 또는 @Profile("local")로 제한하는 것을 권장
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev") // 로컬 환경에서만 실행
public class ElasticsearchIndexInitializer implements CommandLineRunner {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(String... args) {
        log.info("Elasticsearch 인덱스 초기화 시작");

        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(PostDocument.class);

            // 기존 인덱스가 있으면 삭제
            if (indexOps.exists()) {
                log.info("기존 'posts' 인덱스 삭제");
                indexOps.delete();
            }

            // 새 인덱스 생성
            log.info("새 'posts' 인덱스 생성");
            indexOps.create();

            // 매핑 설정
            indexOps.putMapping(indexOps.createMapping(PostDocument.class));

            log.info("Elasticsearch 인덱스 초기화 완료");
        } catch (Exception e) {
            log.error("Elasticsearch 인덱스 초기화 실패", e);
        }
    }
}