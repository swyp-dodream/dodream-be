package swyp.dodream.domain.post.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.post.domain.Bookmark;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.dto.PostSummaryResponse;
import swyp.dodream.domain.post.repository.BookmarkRepository;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SnowflakeIdService snowflakeIdService;

    public boolean toggleBookmark(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("모집글을 찾을 수 없습니다."));

        boolean exists = bookmarkRepository.existsByUserAndPost(user, post);
        if (exists) {
            bookmarkRepository.deleteByUserAndPost(user, post);
            return false; // 북마크 해제
        } else {
            // ✅ Snowflake로 ID 생성
            Long bookmarkId = snowflakeIdService.generateId();
            bookmarkRepository.save(Bookmark.of(bookmarkId, user, post));
            return true; // 북마크 등록
        }
    }

    public List<PostSummaryResponse> getBookmarkedPosts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        return bookmarkRepository.findAllByUser(user).stream()
                .map(Bookmark::getPost)
                .map(PostSummaryResponse::fromEntity)
                .toList();
    }
}
