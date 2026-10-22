package com.giahung19.ecommerce_api.dto;
import java.math.BigDecimal;

public class OrderItemResponseDTO {

    private String productName;
    private Integer quantity;
    private BigDecimal priceAtOrder;

    public OrderItemResponseDTO() {}

    public OrderItemResponseDTO(String productName, Integer quantity, BigDecimal priceAtOrder) {
        this.productName = productName;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(BigDecimal priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

}
