package swyp.dodream.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public List<PostResponse> searchPosts(String keyword) {

        List<PostDocument> docs = postDocumentRepository.searchByTitleOrDescription(keyword);

        return docs.stream()
                .map(doc -> postRepository.findById(doc.getId())
                        .orElseThrow(() -> new RuntimeException("POST not found in DB: " + doc.getId()))
                )
                .map(this::toPostResponse)
                .toList();
    }

    private PostResponse toPostResponse(Post post) {
        boolean isOwner = false;

        Profile profile = profileRepository.findByUserId(post.getOwner().getId())
                .orElse(null);

        String ownerNickname = profile != null ? profile.getNickname() : null;

        Integer profileImageCode = profile != null ? profile.getProfileImageCode() : null;
        String ownerProfileImageUrl = profileImageCode != null
                ? profileImageCode.toString()
                : null;

        return PostResponse.from(post, isOwner, ownerNickname, ownerProfileImageUrl);
    }
}