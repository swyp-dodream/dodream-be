package swyp.dodream.domain.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.application.dto.response.MyApplicationDetailResponse;
import swyp.dodream.domain.bookmark.repository.BookmarkRepository;
import swyp.dodream.domain.master.domain.ApplicationStatus;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.application.dto.response.MyApplicationPageResponse;
import swyp.dodream.domain.application.dto.response.MyApplicationResponse;
import swyp.dodream.domain.application.repository.ApplicationRepository;
import swyp.dodream.domain.matched.repository.MatchedRepository;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final BookmarkRepository bookmarkRepository;
    private final MatchedRepository matchedRepository;
    private final ProfileRepository profileRepository;

    /**
     * 내가 지원한 글 목록 조회 (페이지네이션)
     *
     * @param userId 유저 ID
     * @param page   페이지 번호 (0부터)
     * @param size   페이지 크기
     */
    public MyApplicationPageResponse getMyApplications(Long userId, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Application> applications = applicationRepository.findApplicationsByUser(
                userId,
                pageable
        );

        List<MyApplicationResponse> contents = applications.getContent().stream()
                .map(app -> {
                    Long postId = app.getPost().getId();
                    boolean bookmarked = bookmarkRepository.existsByUserIdAndPostId(userId, postId);

                    // 리더의 닉네임 조회
                    Long leaderId = app.getPost().getOwner().getId();
                    String leaderNickname = profileRepository.findByUserId(leaderId)
                            .map(Profile::getNickname)
                            .orElse(app.getPost().getOwner().getName()); // fallback

                    return MyApplicationResponse.fromApplication(app, bookmarked, leaderNickname);
                })
                .toList();

        return MyApplicationPageResponse.of(
                contents,
                applications.getNumber(),
                applications.getSize(),
                applications.getTotalElements(),
                applications.getTotalPages(),
                applications.hasNext()
        );
    }

    /**
     * 내 지원 상세 정보 조회
     *
     * @param userId 유저 ID
     * @param applicationId 지원 ID
     * @return 지원 상세 정보
     */
    public MyApplicationDetailResponse getMyApplicationDetail(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "지원 정보를 찾을 수 없습니다."));

        if (!application.getApplicant().getId().equals(userId)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "본인의 지원 정보만 조회할 수 있습니다.");
        }

        if (application.getStatus() != ApplicationStatus.APPLIED
                && application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new CustomException(ExceptionType.NOT_FOUND, "조회 가능한 상태가 아닙니다.");
        }

        // 리더의 닉네임 조회
        Long leaderId = application.getPost().getOwner().getId();
        String leaderNickname = profileRepository.findByUserId(leaderId)
                .map(Profile::getNickname)
                .orElse(application.getPost().getOwner().getName()); // fallback

        return MyApplicationDetailResponse.fromApplication(application, leaderNickname);
    }

    @Transactional
    public void cancelByApplicant(Long applicationId, Long userId) {
        Application app = applicationRepository.findByIdAndApplicantId(applicationId, userId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "지원 내역을 찾을 수 없습니다."));

        boolean canceledMatchedExists = matchedRepository
                .findByApplicationIdAndIsCanceledTrue(applicationId)
                .isPresent();

        if (canceledMatchedExists) {
            throw ExceptionType.CONFLICT_MATCHED_ALREADY_CANCELED.throwException(
                    "이미 매칭이 취소된 상태에서는 지원 취소가 불가능합니다."
            );
        }

        if (app.getStatus() != ApplicationStatus.APPLIED) {
            throw new CustomException(ExceptionType.BAD_REQUEST_INVALID, "이미 처리된 지원은 취소할 수 없습니다.");
        }

        app.withdraw();
    }
}