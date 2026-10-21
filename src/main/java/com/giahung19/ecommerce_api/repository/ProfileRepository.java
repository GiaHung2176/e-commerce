package com.giahung19.ecommerce_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.giahung19.ecommerce_api.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
