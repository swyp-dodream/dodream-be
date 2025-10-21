package swyp.dodream.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", e.getExceptionType().name());
        response.put("message", e.getMessage());
        
        return ResponseEntity
                .status(e.getExceptionType().getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "SERVER_ERROR");
        response.put("message", "서버 내부 오류가 발생했습니다: " + e.getMessage());
        
        return ResponseEntity
                .status(500)
                .body(response);
    }
}

