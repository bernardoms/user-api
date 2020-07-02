package com.bernardoms.user.controller;

import com.bernardoms.user.exception.NicknameAlreadyExistException;
import com.bernardoms.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler({BindException.class, HttpMessageNotReadableException.class})
    private ResponseEntity<Object> handleIllegalArgumentException(Exception ex, HttpServletRequest request) {
        log.error("invalid arguments/body for processing the request: " + request.getRequestURI(), ex);
        return new ResponseEntity<>(mountError(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NicknameAlreadyExistException.class})
    private ResponseEntity<Object> handleNickNameAlreadyExistException(NicknameAlreadyExistException ex, HttpServletRequest request) {
        log.info("nick name already exist : " + request.getRequestURI(), ex);
        return new ResponseEntity<>(mountError(ex), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler({UserNotFoundException.class})
    private ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        log.info("user not found! : " + request.getRequestURI(), ex);
        return new ResponseEntity<>(mountError(ex), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler({Exception.class})
    private ResponseEntity<Object> handleException(Exception ex, HttpServletRequest request) {
        log.error("error on process the request: " + request.getRequestURI(), ex);
        return new ResponseEntity<>(mountError(ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> details = new HashMap<>();
        var errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        errors.put("description", details);

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    private HashMap<Object, Object> mountError(Exception e) {
        var error = new HashMap<>();
        error.put("description", e.getMessage());
        return error;
    }
}
