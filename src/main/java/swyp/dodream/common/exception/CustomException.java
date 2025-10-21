package swyp.dodream.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    
    private final ExceptionType exceptionType;
    private final String message;

    public CustomException(ExceptionType exceptionType) {
        super(exceptionType.getDefaultMessage());
        this.exceptionType = exceptionType;
        this.message = exceptionType.getDefaultMessage();
    }

    public CustomException(ExceptionType exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
        this.message = message;
    }
}

