package com.giahung19.ecommerce_api.exception;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String message){
        super(message);
    }
}
