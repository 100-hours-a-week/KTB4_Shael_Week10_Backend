package org.example.communityservice.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException{
    public ForbiddenException(){
        super(HttpStatus.FORBIDDEN, "permission_denied");
    }
}
