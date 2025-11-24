package swyp.dodream.domain.search.repository;

import org.springframework.data.elasticsearch.annotations.Query;
import swyp.dodream.domain.search.document.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostDocumentRepository extends ElasticsearchRepository<PostDocument, Long> {

    /**
     * 기본 검색
     * - 대소문자 구분 없음 (lowercase 필터)
     * - 단어 순서 무관 (multi_match)
     * - 오타 허용 (fuzziness AUTO)
     * - 동의어 자동 적용 (my_synonyms 필터)
     * - 한글/영어 모두 지원 (nori_tokenizer)
     *
     * 예시:
     * - "spring" 검색 → "스프링", "Spring" 모두 검색됨
     * - "프론트" 검색 → "frontend", "front-end" 모두 검색됨
     * - "자바" 검색 → "java", "Java" 모두 검색됨
     */
    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["title^2", "description"],
                "type": "best_fields",
                "operator": "or",
                "fuzziness": "AUTO",
                "prefix_length": 0,
                "max_expansions": 50,
                "fuzzy_transpositions": true
              }
            }
            """)
    List<PostDocument> searchByKeyword(String keyword);

    /**
     * 엄격한 검색: 모든 단어가 포함되어야 함
     * 동의어는 여전히 적용됨
     */
    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["title^2", "description"],
                "type": "cross_fields",
                "operator": "and",
                "fuzziness": "AUTO"
              }
            }
            """)
    List<PostDocument> searchByKeywordStrict(String keyword);

    /**
     * 퍼지 검색: 오타에 더 관대
     * fuzziness를 2로 설정하여 최대 2글자까지 오타 허용
     */
    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["title^2", "description"],
                "type": "best_fields",
                "operator": "or",
                "fuzziness": "2",
                "prefix_length": 0,
                "max_expansions": 100
              }
            }
            """)
    List<PostDocument> searchWithHighFuzziness(String keyword);

    /**
     * 제목만 검색 (제목에서만 찾고 싶을 때)
     */
    @Query("""
            {
              "match": {
                "title": {
                  "query": "?0",
                  "operator": "or",
                  "fuzziness": "AUTO"
                }
              }
            }
            """)
    List<PostDocument> searchByTitle(String keyword);
}