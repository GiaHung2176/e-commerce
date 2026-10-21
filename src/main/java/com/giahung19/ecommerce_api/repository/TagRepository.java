package com.giahung19.ecommerce_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.giahung19.ecommerce_api.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
