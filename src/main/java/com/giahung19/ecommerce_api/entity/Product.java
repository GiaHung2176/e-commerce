package com.giahung19.ecommerce_api.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    private Integer stockQuantity;

    // Quan hệ Nhiều-1 với Category (Khóa ngoại)
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany
    @JoinTable(
        name = "product_tags",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags=new HashSet<>();

    public Product() {
    }

    public Product(String name, BigDecimal price, Integer stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<Tag> getTags(){
        return this.tags;
    }
    public void setTags(Set<Tag> tags){
        this.tags=tags;
    }

    public boolean addTag(Tag tag){
        if(tag!=null){
            tags.add(tag);
            tag.getProducts().add(this);
            return true;
        }
        return false;
    }

    public boolean removeTag(Tag tag){
        if(tag!=null&&tags.remove(tag)){
            tag.getProducts().remove(this);
            return true;
        }
        return false;
    }
}