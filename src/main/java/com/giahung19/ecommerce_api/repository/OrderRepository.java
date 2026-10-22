package com.giahung19.ecommerce_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.giahung19.ecommerce_api.entity.Order;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query ("select o from Order o join fetch o.user u left join fetch u.profile join fetch o.items i join fetch i.product")
    public List<Order> findAllWithUserAndItems();

    @Query ("select o from Order o join fetch o.user u join fetch o.items i join fetch i.product where u.username = :username")
    public List<Order> findAllByUsernameWithItems(@Param("username") String username);

}