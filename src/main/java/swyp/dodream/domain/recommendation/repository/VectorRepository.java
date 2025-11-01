package swyp.dodream.domain.recommendation.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Qdrant 벡터 데이터베이스 연동 Repository
 * REST API를 통해 벡터 저장 및 검색 기능 제공
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class VectorRepository {

    private final ObjectMapper objectMapper;

    @Value("${qdrant.collection-name}")
    private String collectionName;

    @Value("${qdrant.vector-size}")
    private int vectorSize;

    @Value("${qdrant.host}")
    private String host;

    @Value("${qdrant.port}")
    private int port;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private String getBaseUrl() {
        return String.format("http://%s:%d", host, port);
    }

    /**
     * 컬렉션이 존재하는지 확인하고 없으면 생성
     */
    public void ensureCollectionExists() {
        try {
            // 컬렉션 조회
            Request request = new Request.Builder()
                    .url(String.format("%s/collections/%s", getBaseUrl(), collectionName))
                    .get()
                    .build();

            Response response = httpClient.newCall(request).execute();
            
            if (response.code() == 404) {
                // 컬렉션이 없으면 생성
                createCollection();
            } else if (!response.isSuccessful()) {
                log.error("컬렉션 확인 실패: {}", response.body().string());
                throw new IllegalStateException("컬렉션 확인 실패");
            }
            
            response.close();
        } catch (IOException e) {
            log.error("컬렉션 확인 중 오류", e);
            throw new IllegalStateException("컬렉션 확인 실패", e);
        }
    }

    /**
     * 컬렉션 생성
     */
    private void createCollection() {
        try {
            Map<String, Object> body = Map.of(
                    "vectors", Map.of(
                            "size", vectorSize,
                            "distance", "Cosine"
                    )
            );

            String json = objectMapper.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url(String.format("%s/collections/%s", getBaseUrl(), collectionName))
                    .put(RequestBody.create(json, MediaType.get("application/json")))
                    .build();

            Response response = httpClient.newCall(request).execute();
            
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("컬렉션 생성 실패: {}", errorBody);
                throw new IllegalStateException("컬렉션 생성 실패");
            }
            
            response.close();
            log.info("Qdrant 컬렉션 생성 완료: {}", collectionName);
        } catch (IOException e) {
            log.error("컬렉션 생성 중 오류", e);
            throw new IllegalStateException("컬렉션 생성 실패", e);
        }
    }

    /**
     * 벡터 저장
     * @param postId 게시글 ID
     * @param embedding 벡터
     */
    public void upsertVector(Long postId, float[] embedding) {
        upsertVector(postId, embedding, null);
    }

    /**
     * 벡터 저장 (payload 포함)
     * @param postId 게시글 ID
     * @param embedding 벡터
     * @param payload 메타데이터
     */
    public void upsertVector(Long postId, float[] embedding, Map<String, Object> payload) {
        ensureCollectionExists();
        
        try {
            Map<String, Object> point = new HashMap<>();
            point.put("id", postId);
            point.put("vector", embedding);
            
            // payload가 있으면 추가
            if (payload != null) {
                point.put("payload", payload);
            }

            Map<String, Object> body = Map.of(
                    "points", List.of(point)
            );

            String json = objectMapper.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url(String.format("%s/collections/%s/points", getBaseUrl(), collectionName))
                    .put(RequestBody.create(json, MediaType.get("application/json")))
                    .build();

            Response response = httpClient.newCall(request).execute();
            
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("벡터 저장 실패: {}", errorBody);
                throw new IllegalStateException("벡터 저장 실패");
            }
            
            response.close();
            log.debug("벡터 저장 완료: postId={}", postId);
        } catch (IOException e) {
            log.error("벡터 저장 중 오류", e);
            throw new IllegalStateException("벡터 저장 실패", e);
        }
    }

    /**
     * 벡터 검색 (유사도 기반)
     * @param queryEmbedding 검색 쿼리 벡터
     * @param limit 결과 개수
     * @return 검색된 postId 목록
     */
    public List<Long> searchSimilar(float[] queryEmbedding, int limit) {
        try {
            log.debug("벡터 검색 시작: limit={}", limit);
            Map<String, Object> body = Map.of(
                    "vector", queryEmbedding,
                    "limit", limit,
                    "with_payload", false
            );

            String json = objectMapper.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url(String.format("%s/collections/%s/points/search", getBaseUrl(), collectionName))
                    .post(RequestBody.create(json, MediaType.get("application/json")))
                    .build();

            Response response = httpClient.newCall(request).execute();
            
            log.debug("Qdrant 응답 상태: {}", response.code());
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("벡터 검색 실패: {}", errorBody);
                throw new IllegalStateException("벡터 검색 실패");
            }
            
            String responseBody = response.body().string();
            response.close();

            // 응답 파싱
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode result = root.get("result");
            
            List<Long> postIds = new ArrayList<>();
            if (result != null && result.isArray()) {
                for (JsonNode point : result) {
                    JsonNode id = point.get("id");
                    if (id != null && id.isNumber()) {
                        postIds.add(id.asLong());
                    }
                }
            }

            log.debug("벡터 검색 완료: {}개 결과", postIds.size());
            return postIds;

        } catch (IOException e) {
            log.error("벡터 검색 중 오류", e);
            throw new IllegalStateException("벡터 검색 실패", e);
        }
    }

    /**
     * 벡터 삭제
     */
    public void deleteVector(Long postId) {
        try {
            Map<String, Object> body = Map.of(
                    "points", List.of(postId)
            );

            String json = objectMapper.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url(String.format("%s/collections/%s/points/delete", getBaseUrl(), collectionName))
                    .post(RequestBody.create(json, MediaType.get("application/json")))
                    .build();

            Response response = httpClient.newCall(request).execute();
            
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("벡터 삭제 실패: {}", errorBody);
                throw new IllegalStateException("벡터 삭제 실패");
            }
            
            response.close();
            log.debug("벡터 삭제 완료: postId={}", postId);
        } catch (IOException e) {
            log.error("벡터 삭제 중 오류", e);
            throw new IllegalStateException("벡터 삭제 실패", e);
        }
    }
}

