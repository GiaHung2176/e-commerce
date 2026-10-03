package com.giahung19.ecommerce_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.giahung19.ecommerce_api.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByCategoryId(Long categoryId);
}