package swyp.dodream.domain.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.common.snowflake.SnowflakeIdService;
import swyp.dodream.domain.master.domain.ApplicationStatus;
import swyp.dodream.domain.application.domain.Application;
import swyp.dodream.domain.matched.domain.Matched;
import swyp.dodream.domain.post.domain.Suggestion;
import swyp.dodream.domain.post.dto.res.MyApplicationDetailResponse;
import swyp.dodream.domain.post.dto.res.MyApplicationListResponse;
import swyp.dodream.domain.post.dto.res.MyApplicationResponse;
import swyp.dodream.domain.application.repository.ApplicationRepository;
import swyp.dodream.domain.matched.repository.MatchedRepository;
import swyp.dodream.domain.post.repository.SuggestionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final SuggestionRepository suggestionRepository;
    private final MatchedRepository matchedRepository;
    private final SnowflakeIdService snowflakeIdService;

    /**
     * 내가 지원한 글 목록 조회
     *
     * @param userId 유저 ID
     * @param cursor 커서 (다음 페이지용)
     * @param size 페이지 크기
     * @return 지원한 글 목록
     */
    public MyApplicationListResponse getMyApplications(Long userId, Long cursor, Integer size) {
        // 1. 지원 목록 조회
        Slice<Application> applications;
        if (cursor == null) {
            applications = applicationRepository.findApplicationsByUser(
                    userId, PageRequest.of(0, size));
        } else {
            applications = applicationRepository.findApplicationsByUserAfterCursor(
                    userId, cursor, PageRequest.of(0, size));
        }

        // 2. DTO 변환
        List<MyApplicationResponse> responses = applications.getContent().stream()
                .map(MyApplicationResponse::fromApplication)
                .collect(Collectors.toList());

        // 3. nextCursor 계산
        Long nextCursor = applications.getContent().isEmpty() ? null :
                applications.getContent().get(applications.getContent().size() - 1).getId();

        return MyApplicationListResponse.of(responses, nextCursor, applications.hasNext());
    }

    /**
     * 내가 제안받은 글 목록 조회
     *
     * @param userId 유저 ID
     * @param cursor 커서 (다음 페이지용)
     * @param size 페이지 크기
     * @return 제안받은 글 목록
     */
    public MyApplicationListResponse getMySuggestions(Long userId, Long cursor, Integer size) {
        // 1. 제안 목록 조회
        Slice<Suggestion> suggestions;
        if (cursor == null) {
            suggestions = suggestionRepository.findSuggestionsByToUser(
                    userId, PageRequest.of(0, size));
        } else {
            suggestions = suggestionRepository.findSuggestionsByToUserAfterCursor(
                    userId, cursor, PageRequest.of(0, size));
        }

        // 2. DTO 변환
        List<MyApplicationResponse> responses = suggestions.getContent().stream()
                .map(MyApplicationResponse::fromSuggestion)
                .collect(Collectors.toList());

        // 3. nextCursor 계산
        Long nextCursor = suggestions.getContent().isEmpty() ? null :
                suggestions.getContent().get(suggestions.getContent().size() - 1).getId();

        return MyApplicationListResponse.of(responses, nextCursor, suggestions.hasNext());
    }

    /**
     * 내가 매칭된 글 목록 조회
     *
     * @param userId 유저 ID
     * @param cursor 커서 (다음 페이지용)
     * @param size 페이지 크기
     * @return 매칭된 글 목록
     */
    public MyApplicationListResponse getMyMatched(Long userId, Long cursor, Integer size) {
        // 1. 매칭 목록 조회
        Slice<Matched> matched;
        if (cursor == null) {
            matched = matchedRepository.findMatchedByUser(
                    userId, PageRequest.of(0, size));
        } else {
            matched = matchedRepository.findMatchedByUserAfterCursor(
                    userId, cursor, PageRequest.of(0, size));
        }

        // 2. DTO 변환
        List<MyApplicationResponse> responses = matched.getContent().stream()
                .map(MyApplicationResponse::fromMatched)
                .collect(Collectors.toList());

        // 3. nextCursor 계산
        Long nextCursor = matched.getContent().isEmpty() ? null :
                matched.getContent().get(matched.getContent().size() - 1).getId();

        return MyApplicationListResponse.of(responses, nextCursor, matched.hasNext());
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