package swyp.dodream.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Qdrant 벡터 데이터베이스 설정 (REST API 사용)
 */
@Configuration
public class QdrantConfig {

    @Value("${qdrant.host}")
    private String host;

    @Value("${qdrant.port}")
    private int port;

    @Value("${qdrant.timeout:30}")
    private int timeoutSeconds;

    /**
     * Qdrant REST API 베이스 URL
     */
    @Bean
    public String qdrantBaseUrl() {
        return String.format("http://%s:%d", host, port);
    }

    /**
     * Qdrant 타임아웃 설정 (초)
     */
    @Bean
    public int qdrantTimeout() {
        return timeoutSeconds;
    }
}
