package org.example.communityservice.common;

import org.example.communityservice.common.Exception.BusinessException;
import org.example.communityservice.common.dto.CommonResponseDto;
import org.example.communityservice.common.dto.ErrorInfoDto;
import org.example.communityservice.common.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponseDto<Object>> handleBusinessException(BusinessException e){
        return ResponseEntity.status(e.getHttpStatus()).body(new CommonResponseDto<>(e.getMessage(), e.getData()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponseDto<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        List<ErrorInfoDto> errorList = new ArrayList<>();
        for(FieldError error : e.getBindingResult().getFieldErrors()){
            errorList.add(new ErrorInfoDto(error.getField(), error.getDefaultMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponseDto<>("invalid_request", new ErrorResponseDto(errorList)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CommonResponseDto<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e){
        ErrorInfoDto error = new ErrorInfoDto(e.getName(), "type_mismatch");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponseDto<>("invalid_request", new ErrorResponseDto(List.of(error))));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResponseDto<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
        ErrorInfoDto error = new ErrorInfoDto(null, "invalid_request_body");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponseDto<>("invalid_request", new ErrorResponseDto(List.of(error))));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CommonResponseDto<Object>> handleNoResourceFoundException(NoResourceFoundException e){
        ErrorInfoDto error = new ErrorInfoDto(e.getHttpMethod().toString(), e.getResourcePath());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponseDto<>("invalid_request", new ErrorResponseDto(List.of(error))));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponseDto<Object>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CommonResponseDto<>("internal_server_error", null));
    }
}
