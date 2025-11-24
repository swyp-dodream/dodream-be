package swyp.dodream.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swyp.dodream.domain.bookmark.repository.BookmarkRepository;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.dto.response.PostResponse;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.search.document.PostDocument;
import swyp.dodream.domain.search.repository.PostDocumentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final PostDocumentRepository postDocumentRepository;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final BookmarkRepository bookmarkRepository;

    /**
     * 게시글 검색
     * - 대소문자 구분 없음
     * - 단어 순서 무관
     * - 오타 허용 (AUTO fuzziness)
     * - 한글/영어 모두 지원
     */
    public List<PostResponse> searchPosts(String keyword, Long userId) {
        log.debug("검색 키워드: {}, 사용자 ID: {}", keyword, userId);

        // 1. Elasticsearch에서 검색
        List<PostDocument> docs = postDocumentRepository.searchByKeyword(keyword);

        log.debug("Elasticsearch 검색 결과: {}건", docs.size());

        // 2. DB에서 전체 정보 조회 및 응답 변환
        return docs.stream()
                .map(doc -> postRepository.findById(doc.getId())
                        .orElse(null))
                .filter(post -> post != null) // null 필터링
                .map(post -> toPostResponse(post, userId))
                .collect(Collectors.toList());
    }

    /**
     * 엄격한 검색 (모든 키워드가 포함되어야 함)
     */
    public List<PostResponse> searchPostsStrict(String keyword, Long userId) {
        List<PostDocument> docs = postDocumentRepository.searchByKeywordStrict(keyword);

        return docs.stream()
                .map(doc -> postRepository.findById(doc.getId())
                        .orElse(null))
                .filter(post -> post != null)
                .map(post -> toPostResponse(post, userId))
                .collect(Collectors.toList());
    }

    /**
     * 오타에 더 관대한 검색
     */
    public List<PostResponse> searchPostsWithHighFuzziness(String keyword, Long userId) {
        List<PostDocument> docs = postDocumentRepository.searchWithHighFuzziness(keyword);

        return docs.stream()
                .map(doc -> postRepository.findById(doc.getId())
                        .orElse(null))
                .filter(post -> post != null)
                .map(post -> toPostResponse(post, userId))
                .collect(Collectors.toList());
    }

    private PostResponse toPostResponse(Post post, Long userId) {
        boolean isOwner = userId != null && post.getOwner().getId().equals(userId);

        Profile profile = profileRepository.findByUserId(post.getOwner().getId())
                .orElse(null);

        String ownerNickname = profile != null ? profile.getNickname() : null;

        Integer profileImageCode = profile != null ? profile.getProfileImageCode() : null;
        String ownerProfileImageUrl = profileImageCode != null
                ? profileImageCode.toString()
                : null;

        Boolean isBookmarked = false;
        if (userId != null) {
            isBookmarked = bookmarkRepository.existsByUserIdAndPostId(userId, post.getId());
        }

        return PostResponse.from(
                post,
                isOwner,
                ownerNickname,
                ownerProfileImageUrl,
                null,
                null,
                isBookmarked
        );
    }
}