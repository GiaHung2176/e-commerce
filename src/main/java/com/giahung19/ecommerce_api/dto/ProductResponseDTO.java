package com.giahung19.ecommerce_api.dto;
import java.math.BigDecimal;

public class ProductResponseDTO {
    
    private Long id;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;

    public ProductResponseDTO() {}

    public ProductResponseDTO(Long id,Long categoryId,String name, BigDecimal price, Integer stockQuantity) {
        this.id=id;
        this.categoryId=categoryId;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public Long getId(){
        return this.id;
    }

    public void setId(Long id){
        this.id=id;
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
