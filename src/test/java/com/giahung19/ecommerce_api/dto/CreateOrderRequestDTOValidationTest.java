package com.giahung19.ecommerce_api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateOrderRequestDTOValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    // 🧪 Test case 1: hai dòng cùng productId trong một đơn -> có lỗi ở field items
    @Test
    void validate_WhenItemsContainDuplicateProductId_ShouldReportViolationOnItems() {
        CreateOrderRequestDTO requestDTO = new CreateOrderRequestDTO(List.of(
                new OrderItemRequestDTO(1L, 2),
                new OrderItemRequestDTO(1L, 3)));

        Set<ConstraintViolation<CreateOrderRequestDTO>> violations = validator.validate(requestDTO);

        assertEquals(1, violations.size());
        assertEquals("items", violations.iterator().next().getPropertyPath().toString());
    }

    // 🧪 Test case 2: các productId khác nhau -> hợp lệ (quy tắc mới không được chặn nhầm đơn bình thường)
    @Test
    void validate_WhenItemsHaveDistinctProductIds_ShouldHaveNoViolation() {
        CreateOrderRequestDTO requestDTO = new CreateOrderRequestDTO(List.of(
                new OrderItemRequestDTO(1L, 2),
                new OrderItemRequestDTO(2L, 3)));

        Set<ConstraintViolation<CreateOrderRequestDTO>> violations = validator.validate(requestDTO);

        assertTrue(violations.isEmpty());
    }
}
