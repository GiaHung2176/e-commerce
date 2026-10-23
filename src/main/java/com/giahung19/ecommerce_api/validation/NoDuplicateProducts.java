package com.giahung19.ecommerce_api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = NoDuplicateProductsValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoDuplicateProducts {
    String message() default "Order items must not contain duplicate products";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
