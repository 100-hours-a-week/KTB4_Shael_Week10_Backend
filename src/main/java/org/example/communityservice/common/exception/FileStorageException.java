package org.example.communityservice.common.exception;

import org.springframework.http.HttpStatus;

public class FileStorageException extends BusinessException{
    public FileStorageException(){
        super(HttpStatus.INTERNAL_SERVER_ERROR, "file_storage_failed");
    }
}
