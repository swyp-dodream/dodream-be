package swyp.dodream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // AI 기능에서 사용합니다
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}