package com.giahung19.ecommerce_api.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse error = new ErrorResponse(status.value(), message, LocalDateTime.now());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exc) {
        return build(HttpStatus.NOT_FOUND, exc.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exc) {
        List <FieldError> fieldErrors= exc.getBindingResult().getFieldErrors();
        List<FieldErrorDetail> fieldErrorsDetail = fieldErrors.stream()
                .map(x -> new FieldErrorDetail(x.getField(), x.getDefaultMessage()))
                .toList();
        ErrorResponse errorResponse =new ErrorResponse(HttpStatus.BAD_REQUEST.value(),"Validation failed",LocalDateTime.now(),fieldErrorsDetail);
    
        return new ResponseEntity<> (errorResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<ErrorResponse> handleDataConflictException(DataConflictException exc) {
        return build(HttpStatus.CONFLICT, exc.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException exc) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException exc) {
        return build(HttpStatus.BAD_REQUEST, exc.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exc) {
        return build(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exc) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + exc.getName() + "'");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException exc) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method '" + exc.getMethod() + "' is not supported");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException exc) {
        return build(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException exc) {
        return build(HttpStatus.CONFLICT, "Data conflict or duplicate value");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exc) {
        log.error("Unexpected error", exc);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    @ExceptionHandler (ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException (ConstraintViolationException exc) {
        Set <ConstraintViolation<?>> fieldErrors= exc.getConstraintViolations();
        List<FieldErrorDetail> fieldErrorsDetail = fieldErrors.stream()
                .map(x -> new FieldErrorDetail(x.getPropertyPath().toString(), x.getMessage()))
                .toList();
        ErrorResponse errorResponse =new ErrorResponse(HttpStatus.BAD_REQUEST.value(),"Validation failed",LocalDateTime.now(),fieldErrorsDetail);
    
        return new ResponseEntity<> (errorResponse,HttpStatus.BAD_REQUEST);
    }
}
