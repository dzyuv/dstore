package com.dzy.common.exceptionHandler;

import com.dzy.common.entity.ResultJSON;
import com.dzy.common.exception.BusinessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResultJSON handleBusinessException(BusinessException e) {
        return ResultJSON.error(e.getCode() == null ? 400 : e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultJSON handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResultJSON.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public ResultJSON handleException(Exception e) {
        e.printStackTrace();
        return ResultJSON.error(500, "系统繁忙：" + e.getMessage());
    }
}
