package swyp.dodream.domain.search.service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class SearchService {

    private final PostDocumentRepository postDocumentRepository;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final BookmarkRepository bookmarkRepository;

    public List<PostResponse> searchPosts(String keyword, Long userId) {

        List<PostDocument> docs = postDocumentRepository.searchByTitleOrDescription(keyword);

        return docs.stream()
                .map(doc -> postRepository.findById(doc.getId())
                        .orElseThrow(() -> new RuntimeException("POST not found in DB: " + doc.getId()))
                )
                .map(post -> toPostResponse(post, userId))
                .toList();
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