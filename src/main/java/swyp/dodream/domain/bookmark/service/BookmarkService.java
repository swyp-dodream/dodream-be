package swyp.dodream.domain.bookmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.bookmark.domain.Bookmark;
import swyp.dodream.domain.bookmark.dto.response.MyBookmarkPageResponse;
import swyp.dodream.domain.bookmark.dto.response.MyBookmarkResponse;
import swyp.dodream.domain.bookmark.repository.BookmarkRepository;
import swyp.dodream.domain.post.domain.Post;
import swyp.dodream.domain.post.repository.PostRepository;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SnowflakeIdService snowflakeIdService;
    private final ProfileRepository profileRepository;

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

    public MyBookmarkPageResponse getBookmarkedPosts(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUserId(userId, pageable);

        Set<Long> leaderIds = bookmarkPage.getContent().stream()
                .map(b -> b.getPost().getOwner().getId())
                .collect(Collectors.toSet());

        Map<Long, Profile> profileMap = profileRepository.findByUserIdIn(leaderIds).stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));

        List<MyBookmarkResponse> content = bookmarkPage.getContent().stream()
                .map(b -> MyBookmarkResponse.from(b, profileMap.get(b.getPost().getOwner().getId())))
                .toList();

        return new MyBookmarkPageResponse(
                content,
                bookmarkPage.getNumber(),
                bookmarkPage.getSize(),
                bookmarkPage.getTotalElements(),
                bookmarkPage.getTotalPages(),
                bookmarkPage.hasNext()
        );
    }
}