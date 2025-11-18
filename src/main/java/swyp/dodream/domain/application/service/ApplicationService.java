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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final BookmarkRepository bookmarkRepository;

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
                    return MyApplicationResponse.fromApplication(app, bookmarked);
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
        // 1. 지원 정보 조회
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "지원 정보를 찾을 수 없습니다."));

        // 2. 본인 지원인지 확인
        if (!application.getApplicant().getId().equals(userId)) {
            throw new CustomException(ExceptionType.FORBIDDEN, "본인의 지원 정보만 조회할 수 있습니다.");
        }

        // 3. 상태 확인: APPLIED 또는 ACCEPTED 상태가 아니면 조회 불가
        if (application.getStatus() != ApplicationStatus.APPLIED
                && application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new CustomException(ExceptionType.NOT_FOUND, "조회 가능한 상태가 아닙니다.");
        }

        // 4. DTO 변환
        return MyApplicationDetailResponse.fromApplication(application);
    }

    @Transactional
    public void cancelByApplicant(Long applicationId, Long userId) {
        Application app = applicationRepository.findByIdAndApplicantId(applicationId, userId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND, "지원 내역을 찾을 수 없습니다."));

        if (app.getStatus() != ApplicationStatus.APPLIED) {
            throw new CustomException(ExceptionType.BAD_REQUEST_INVALID, "이미 처리된 지원은 취소할 수 없습니다.");
        }

        app.withdraw(); // status 변경 + withdrawn_at 기록
        // 알림 없음(요구사항: 리더 알림 X)
    }
}