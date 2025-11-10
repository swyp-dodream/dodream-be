package swyp.dodream.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import swyp.dodream.domain.post.dto.res.PostResponse;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.search.document.PostDocument;
import swyp.dodream.domain.search.repository.PostDocumentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final PostDocumentRepository postDocumentRepository;
    private final PostRepository postRepository;

    public List<PostResponse> searchPosts(String keyword) {

        List<PostDocument> docs = postDocumentRepository.searchWithFuzzy(keyword);

        return docs.stream()
                .map(doc -> postRepository.findById(doc.getId())
                        .orElseThrow(() -> new RuntimeException("POST not found in DB: " + doc.getId()))
                )
                .map(post -> PostResponse.from(post, false))
                .toList();
    }

}

