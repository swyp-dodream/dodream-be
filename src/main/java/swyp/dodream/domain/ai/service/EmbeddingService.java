package swyp.dodream.domain.ai.service;

/**
 * 임베딩 서비스 인터페이스
 * 텍스트를 벡터로 변환하는 기능 제공
 */
public interface EmbeddingService {
    /**
     * 텍스트를 벡터로 변환
     * @param text 변환할 텍스트
     * @return 벡터 배열 (float[])
     */
    float[] embed(String text);
}

