package com.giahung19.ecommerce_api.validation;

import com.giahung19.ecommerce_api.dto.OrderItemRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoDuplicateProductsValidator
        implements ConstraintValidator<NoDuplicateProducts, List<OrderItemRequestDTO>> {

    @Override
    public boolean isValid(List<OrderItemRequestDTO> items, ConstraintValidatorContext context) {
        if (items == null) {
            return true; // thiếu danh sách đã có @NotEmpty báo lỗi, không báo trùng lặp ở đây
        }

        Set<Long> seenProductIds = new HashSet<>();
        for (OrderItemRequestDTO item : items) {
            boolean isFirstTimeSeen = seenProductIds.add(item.getProductId());
            if (!isFirstTimeSeen) {
                return false;
            }
        }
        return true;
    }
}
