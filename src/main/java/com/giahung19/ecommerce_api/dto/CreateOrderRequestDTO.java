package com.giahung19.ecommerce_api.dto;

import java.util.List;
import com.giahung19.ecommerce_api.validation.NoDuplicateProducts;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class CreateOrderRequestDTO {

    @NotEmpty (message = "Order must have at least one item")
    @Valid
    @NoDuplicateProducts 
    private List<OrderItemRequestDTO> items;

    public CreateOrderRequestDTO() {}

    public CreateOrderRequestDTO(List<OrderItemRequestDTO> items) {
        this.items = items;
    }

    public List<OrderItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequestDTO> items) {
        this.items = items;
    }

}
