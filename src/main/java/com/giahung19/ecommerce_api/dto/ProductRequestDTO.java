package com.giahung19.ecommerce_api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class ProductRequestDTO {
    
    @NotNull (message = "Product must have category")
    private Long categoryId;

    @NotBlank (message = "Name of product not blank")
    private String name;

    @NotNull (message = "Price is not null")
    @DecimalMin (value="0.0",inclusive = false) 
    private BigDecimal price;


    @NotNull 
    @Min (value = 0)
    private Integer stockQuantity;

    public ProductRequestDTO() {}

    public ProductRequestDTO(Long categoryId,String name, BigDecimal price, Integer stockQuantity) {
        this.categoryId=categoryId;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

}
