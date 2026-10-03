package com.giahung19.ecommerce_api.exception;


public class DataConflictException extends RuntimeException {
    public DataConflictException(String message){
        super(message);
    }
}
