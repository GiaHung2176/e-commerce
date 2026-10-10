package com.giahung19.ecommerce_api.exception;

import java.time.LocalDateTime;
import java.util.*;


public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private List<FieldErrorDetail> errors;
    
    public ErrorResponse(){}

    public ErrorResponse(int status,String message,LocalDateTime timestamp,List<FieldErrorDetail> errors){
        this.status=status;
        this.message=message;
        this.timestamp=timestamp;
        this.errors=errors;
    }
    public ErrorResponse(int status,String message,LocalDateTime timestamp){
        this.status=status;
        this.message=message;
        this.timestamp=timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setErrors (List<FieldErrorDetail> errors){
        this.errors=errors;
    }

    public List<FieldErrorDetail> getErrors (){
        return this.errors;
    }


}
