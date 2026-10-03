package com.giahung19.ecommerce_api.exception;

public class DataIntegrityViolationException extends RuntimeException{
    public DataIntegrityViolationException(String message){
        super(message);
    }
}
