package swyp.dodream.domain.ai.service;

public interface AiService {
    /**
     * AI에게 텍스트 생성 요청
     * @param systemPrompt 시스템 프롬프트
     * @param userPrompt 사용자 프롬프트
     * @return 생성된 텍스트
     */
    String generate(String systemPrompt, String userPrompt);
}