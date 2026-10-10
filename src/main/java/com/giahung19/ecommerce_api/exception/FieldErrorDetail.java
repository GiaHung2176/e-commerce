package com.giahung19.ecommerce_api.exception;

public class FieldErrorDetail {
    
    private String field;
    private String message;

    // Constructor nhận 2 tham số
    public FieldErrorDetail(String field, String message) {
        this.field = field;
        this.message = message;
    }

    // Getter cho field
    public String getField() {
        return field;
    }

    // Getter cho message
    public String getMessage() {
        return message;
    }
}