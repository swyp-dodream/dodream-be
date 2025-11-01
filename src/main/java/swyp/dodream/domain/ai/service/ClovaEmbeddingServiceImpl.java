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
import java.util.UUID;

/**
 * NCP Clova Embedding API 구현
 * 텍스트를 벡터로 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "ncp", matchIfMissing = false)
public class ClovaEmbeddingServiceImpl implements EmbeddingService {

    @Value("${clova.embedding.api-key:}")
    private String apiKey;

    @Value("${clova.embedding.api-gw-key:}")
    private String apiGwKey;

    @Value("${ncp.api-key:}")
    private String ncpApiKey;

    @Value("${clova.embedding.base-url}")
    private String baseUrl;

    @Value("${clova.embedding.model}")
    private String model;

    private final RestTemplate restTemplate;

    @Override
    public float[] embed(String text) {
        log.info("Clova Embedding API 호출 시작");
        log.info("Base URL: {}", baseUrl);

        // Embedding 키가 없으면 기본 NCP 키 사용
        String actualApiKey = !apiKey.isEmpty() ? apiKey : ncpApiKey;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + actualApiKey);
            headers.set("X-NCP-CLOVASTUDIO-REQUEST-ID", generateRequestId());

            Map<String, Object> body = Map.of(
                    "text", text
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, entity, Map.class);

            log.info("응답 상태: {}", response.getStatusCode());

            return extractEmbedding(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("HTTP 에러 발생!");
            log.error("상태 코드: {}", e.getStatusCode());
            log.error("응답 본문: {}", e.getResponseBodyAsString());
            throw new IllegalStateException("Clova Embedding API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("예외 발생", e);
            throw new IllegalStateException("Clova Embedding API 처리 실패: " + e.getMessage());
        }
    }

    /**
     * 응답에서 임베딩 벡터 추출
     * OpenAI 호환 형식 지원
     */
    @SuppressWarnings("unchecked")
    private float[] extractEmbedding(Map<String, Object> responseBody) {
        if (responseBody == null) {
            throw new IllegalStateException("Clova 응답이 비어있습니다");
        }

        log.debug("Clova Embedding 응답 파싱 시작");

        // OpenAI 호환 형식 시도
        if (responseBody.containsKey("data")) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
            if (data != null && !data.isEmpty()) {
                Map<String, Object> firstItem = data.get(0);
                List<Double> embedding = (List<Double>) firstItem.get("embedding");
                if (embedding != null && !embedding.isEmpty()) {
                    return convertToFloatArray(embedding);
                }
            }
        }

        // Clova 고유 형식 시도
        if (responseBody.containsKey("result")) {
            Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
            if (result != null) {
                List<Double> embedding = (List<Double>) result.get("embedding");
                if (embedding != null && !embedding.isEmpty()) {
                    return convertToFloatArray(embedding);
                }
            }
        }

        // 응답 구조가 다를 수 있으므로 전체 로깅
        log.error("Clova Embedding 응답 구조를 파악할 수 없습니다. 응답: {}", responseBody);
        throw new IllegalStateException("Clova 응답에서 embedding을 찾을 수 없습니다");
    }

    private float[] convertToFloatArray(List<Double> embedding) {
        float[] embeddingArray = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            embeddingArray[i] = embedding.get(i).floatValue();
        }
        log.info("임베딩 생성 완료: {}차원", embeddingArray.length);
        return embeddingArray;
    }

    /**
     * Request ID 생성
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}

