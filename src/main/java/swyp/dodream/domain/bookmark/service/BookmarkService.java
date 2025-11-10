package swyp.dodream.domain.bookmark.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.bookmark.domain.Bookmark;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.dto.res.PostSummaryResponse;
import swyp.dodream.domain.bookmark.repository.BookmarkRepository;
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
                .orElseThrow(ExceptionType.USER_NOT_FOUND::throwException);
        Post post = postRepository.findById(postId)
                .orElseThrow(ExceptionType.POST_NOT_FOUND::throwException);

        boolean exists = bookmarkRepository.existsByUserAndPost(user, post);
        if (exists) {
            bookmarkRepository.deleteByUserAndPost(user, post);
            return false; // 북마크 해제
        } else {
            Long bookmarkId = snowflakeIdService.generateId();
            bookmarkRepository.save(Bookmark.of(bookmarkId, user, post));
            return true; // 북마크 등록
        }
    }

    public List<PostSummaryResponse> getBookmarkedPosts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(ExceptionType.USER_NOT_FOUND::throwException);
        return bookmarkRepository.findAllByUser(user).stream()
                .map(Bookmark::getPost)
                .map(PostSummaryResponse::fromEntity)
                .toList();
    }
}
