package swyp.dodream.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionType {
    // Success(200)
    OK(HttpStatus.OK, "Success"),

    // BAD_REQUEST(400)
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청"),
    BAD_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 요청"),

    // UNAUTHORIZED(401)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 실패"),
    UNAUTHORIZED_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰"),
    UNAUTHORIZED_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰"),
    UNAUTHORIZED_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "토큰이 없습니다"),
    UNAUTHORIZED_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token"),
    UNAUTHORIZED_NO_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다"),

    // Forbidden(403)
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),

    // NOT_FOUND(404)
    NOT_FOUND(HttpStatus.NOT_FOUND, "데이터가 존재하지 않음"),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다"),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "직군을 찾을 수 없습니다"),
    TECH_SKILL_NOT_FOUND(HttpStatus.NOT_FOUND, "기술 스킬을 찾을 수 없습니다"),
    TECH_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "기술 카테고리를 찾을 수 없습니다"),
    TECH_STACK_NOT_FOUND(HttpStatus.NOT_FOUND, "기술 스택을 찾을 수 없습니다"),
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "관심 분야를 찾을 수 없습니다"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "모집글을 찾을 수 없습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "지원 내역을 찾을 수 없습니다"),
    MATCHED_NOT_FOUND(HttpStatus.NOT_FOUND, "매칭 내역을 찾을 수 없습니다"),
    MASTER_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "마스터 데이터를 찾을 수 없습니다"),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다"),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다"),
    BADGE_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "배지 상태를 찾을 수 없습니다"),

    // Conflict(409)
    CONFLICT_DUPLICATE(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다"),
    USER_ALREADY_WITHDRAWN(HttpStatus.CONFLICT, "이미 탈퇴한 사용자입니다"),
    DUPLICATE_TECH_STACK(HttpStatus.CONFLICT, "이미 추가된 기술 스택입니다"),
    DUPLICATE_INTEREST(HttpStatus.CONFLICT, "이미 추가된 관심 분야입니다"),

    // Internal Server Error(500)
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러"),
    ;

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    public CustomException of() {
        return new CustomException(this);
    }

    public CustomException of(String message) {
        return new CustomException(this, message);
    }

    public CustomException throwException() {
        return new CustomException(this);
    }

    public CustomException throwException(String message) {
        return new CustomException(this, message);
    }
}

