package swyp.dodream.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.search.document.PostDocument;
import swyp.dodream.domain.search.repository.PostDocumentRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DB의 게시글을 Elasticsearch에 동기화하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostIndexService {

    private final PostRepository postRepository;
    private final PostDocumentRepository postDocumentRepository;

    /**
     * 단일 게시글을 Elasticsearch에 인덱싱
     */
    public void indexPost(Post post) {
        PostDocument document = PostDocument.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getContent())
                .build();

        postDocumentRepository.save(document);
        log.debug("게시글 인덱싱 완료: ID={}, 제목={}", post.getId(), post.getTitle());
    }

    /**
     * 모든 게시글을 Elasticsearch에 재인덱싱
     * 초기 데이터 동기화 또는 인덱스 재구성 시 사용
     */
    @Transactional(readOnly = true)
    public void reindexAllPosts() {
        log.info("전체 게시글 재인덱싱 시작");

        // 기존 인덱스 전체 삭제
        postDocumentRepository.deleteAll();

        // DB의 모든 게시글 조회
        List<Post> allPosts = postRepository.findAll();

        // PostDocument로 변환
        List<PostDocument> documents = allPosts.stream()
                .map(post -> PostDocument.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .description(post.getContent())
                        .build())
                .collect(Collectors.toList());

        // 일괄 저장
        postDocumentRepository.saveAll(documents);

        log.info("전체 게시글 재인덱싱 완료: {}건", documents.size());
    }

    /**
     * 게시글 삭제 시 Elasticsearch에서도 삭제
     */
    public void deletePost(Long postId) {
        postDocumentRepository.deleteById(postId);
        log.debug("게시글 인덱스 삭제: ID={}", postId);
    }
}