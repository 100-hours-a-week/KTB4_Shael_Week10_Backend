package org.example.communityservice.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {
    public NotFoundException(String message, Object data){
        super(HttpStatus.NOT_FOUND, message, data);
    }
}
