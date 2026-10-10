package com.giahung19.ecommerce_api.exception;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice  
public class GlobalException {
    
    @ExceptionHandler 
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exc){
        ErrorResponse error= new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            exc.getMessage(),
            LocalDateTime.now()
        );
        return new ResponseEntity<> (error,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler 
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exc){
        String errorMessage = exc.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        ErrorResponse error =new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            errorMessage,
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler 
    public ResponseEntity<ErrorResponse> handleDataConflictException(DataConflictException exc){
        ErrorResponse error =new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            exc.getMessage(),
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error,HttpStatus.CONFLICT);
    }

    @ExceptionHandler 
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException exc){
        ErrorResponse error =new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            exc.getMessage(),
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler (IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exc){
        ErrorResponse error =new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            exc.getMessage(),
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler (DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException exc){
        ErrorResponse error=new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Data conflict or duplicate value",
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error,HttpStatus.CONFLICT);
    }

    @ExceptionHandler (Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exc){
        ErrorResponse error =new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Unexpected error: "+exc.getMessage(),
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error,HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
