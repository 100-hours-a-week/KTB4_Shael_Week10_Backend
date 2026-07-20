package org.example.communityservice.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final Object data;

    public BusinessException(HttpStatus httpStatus, String message){
        super(message);
        this.httpStatus = httpStatus;
        this.data = null;
    }

    public BusinessException(HttpStatus httpStatus, String message, Object data){
        super(message);
        this.httpStatus = httpStatus;
        this.data = data;
    }
}
