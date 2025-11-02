package swyp.dodream.domain.search.repository;

import swyp.dodream.domain.search.document.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostDocumentRepository extends ElasticsearchRepository<PostDocument, Long> {
}

