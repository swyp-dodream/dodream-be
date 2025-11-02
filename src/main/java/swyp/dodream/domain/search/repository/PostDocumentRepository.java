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
                  { "match": { "content": "?0" } }
                ]
              }
            }
            """)
    List<PostDocument> searchByTitleOrContent(String keyword);

}

