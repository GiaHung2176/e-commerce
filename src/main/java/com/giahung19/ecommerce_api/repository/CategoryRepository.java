package com.giahung19.ecommerce_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.giahung19.ecommerce_api.entity.*;
public interface CategoryRepository extends JpaRepository<Category,Long>{
    boolean existsByName(String name);
}
