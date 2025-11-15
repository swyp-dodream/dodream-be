package swyp.dodream.domain.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.master.domain.ApplicationStatus;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.post.domain.Suggestion;
import swyp.dodream.domain.post.dto.response.MyApplicationDetailResponse;
import swyp.dodream.domain.post.dto.response.MyApplicationPageResponse;
import swyp.dodream.domain.post.dto.response.MyApplicationResponse;
import swyp.dodream.domain.application.repository.ApplicationRepository;
import swyp.dodream.domain.matched.repository.MatchedRepository;
import swyp.dodream.domain.post.repository.SuggestionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final SuggestionRepository suggestionRepository;
    private final MatchedRepository matchedRepository;
    private final SnowflakeIdService snowflakeIdService;

    /**
     * 내가 지원한 글 목록 조회 (페이지네이션)
     *
     * @param userId 유저 ID
     * @param page   페이지 번호 (0부터)
     * @param size   페이지 크기
     */
    public MyApplicationPageResponse getMyApplications(Long userId, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        // 1. 지원 목록 조회 (Page로)
        Page<Application> applications = applicationRepository.findApplicationsByUser(
                userId,
                pageable
        );

        // 2. DTO 변환
        List<MyApplicationResponse> contents = applications.getContent().stream()
                .map(MyApplicationResponse::fromApplication)
                .toList();

        // 3. 페이지 정보 묶어서 반환
        return MyApplicationPageResponse.of(
                contents,
                applications.getNumber(),        // 현재 페이지
                applications.getSize(),          // 페이지 크기
                applications.getTotalElements(), // 전체 개수
                applications.getTotalPages(),    // 전체 페이지 수
                applications.hasNext()           // 다음 페이지 있는지
        );
    }


    /**
     * 내가 제안받은 글 목록 조회 (페이지네이션)
     *
     * @param userId 유저 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     */
    public MyApplicationPageResponse getMySuggestions(Long userId, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 1. 제안 목록 조회 (Page로)
        Page<Suggestion> suggestions = suggestionRepository.findSuggestionsByToUser(userId, pageable);

        // 2. DTO 변환
        List<MyApplicationResponse> contents = suggestions.getContent().stream()
                .map(MyApplicationResponse::fromSuggestion)
                .toList();

        // 3. 페이지 응답으로 감싸서 반환
        return MyApplicationPageResponse.of(
                contents,
                suggestions.getNumber(),
                suggestions.getSize(),
                suggestions.getTotalElements(),
                suggestions.getTotalPages(),
                suggestions.hasNext()
        );
    }


    /**
     * 내가 매칭된 글 목록 조회 (페이지네이션)
     *
     * @param userId 유저 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     */
    public MyApplicationPageResponse getMyMatched(Long userId, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "matchedAt"));

        // 1. 매칭 목록 조회 (Page)
        Page<Matched> matchedPage = matchedRepository.findMatchedByUser(userId, pageable);

        // 2. DTO 변환
        List<MyApplicationResponse> contents = matchedPage.getContent().stream()
                .map(MyApplicationResponse::fromMatched)
                .toList();

        // 3. 페이지 응답으로 감싸서 반환
        return MyApplicationPageResponse.of(
                contents,
                matchedPage.getNumber(),
                matchedPage.getSize(),
                matchedPage.getTotalElements(),
                matchedPage.getTotalPages(),
                matchedPage.hasNext()
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

        // 3. DTO 변환
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