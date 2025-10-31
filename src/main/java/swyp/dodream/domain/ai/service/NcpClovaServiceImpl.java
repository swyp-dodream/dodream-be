package swyp.dodream.domain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "ncp")
public class NcpClovaServiceImpl implements AiService {

    @Value("${ncp.api-key}")
    private String apiKey;

    @Value("${ncp.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        log.info("NCP API 호출 시작");
        log.info("API Key: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));
        log.info("Base URL: {}", baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-CLOVASTUDIO-API-KEY", apiKey);
        headers.set("X-NCP-APIGW-API-KEY", apiKey);

        Map<String, Object> body = Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "topP", 0.8,
                "topK", 0,
                "maxTokens", 256,
                "temperature", 0.5,
                "repeatPenalty", 5.0,
                "stopBefore", List.of(),
                "includeAiFilters", true,
                "seed", 0
        );

        log.info("요청 바디: {}", body);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl, entity, Map.class);

            log.info("응답 상태: {}", resp.getStatusCode());
            log.info("응답 바디: {}", resp.getBody());

            return extractContent(resp.getBody());

        } catch (HttpClientErrorException e) {
            log.error("HTTP 에러 발생!");
            log.error("상태 코드: {}", e.getStatusCode());
            log.error("응답 본문: {}", e.getResponseBodyAsString());
            log.error("헤더: {}", e.getResponseHeaders());
            throw new IllegalStateException("NCP API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("예외 발생", e);
            throw new IllegalStateException("NCP API 처리 실패: " + e.getMessage());
        }
    }

    private String extractContent(Map<String, Object> responseBody) {
        if (responseBody == null) {
            throw new IllegalStateException("NCP 응답이 비어있습니다");
        }

        Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
        if (result == null) {
            throw new IllegalStateException("NCP 응답에 result가 없습니다");
        }

        Map<String, Object> message = (Map<String, Object>) result.get("message");
        if (message == null) {
            throw new IllegalStateException("NCP 응답에 message가 없습니다");
        }

        Object content = message.get("content");
        return content == null ? "" : content.toString();
    }
}