package com.dzy.common.exceptionHandler;

import com.dzy.common.entity.ResultJSON;
import com.dzy.common.exception.BusinessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
        log.error("未捕获异常", e);
        return ResultJSON.error(500, "系统繁忙");
    }
}
