package swyp.dodream.domain.search.repository;

import org.springframework.data.elasticsearch.annotations.Query;
import swyp.dodream.domain.search.document.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostDocumentRepository extends ElasticsearchRepository<PostDocument, Long> {

    @Query("""
            {
              "bool": {
                "should": [
                  { "match": { "title": "?0" } },
                  { "match": { "description": "?0" } }
                ]
              }
            }
            """)
    List<PostDocument> searchByTitleOrDescription(String keyword);

    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["title", "description"],
                "operator": "or",
                "fuzziness": "AUTO"
              }
            }
            """)
    List<PostDocument> searchWithFuzzy(String keyword);
}