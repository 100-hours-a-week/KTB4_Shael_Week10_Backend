package org.example.communityservice.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException{
    public BadRequestException(String message){
        super(HttpStatus.BAD_REQUEST, message);
    }
    public BadRequestException(String message, Object data){
        super(HttpStatus.BAD_REQUEST, message, data);
    }
}
