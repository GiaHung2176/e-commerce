package com.giahung19.ecommerce_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;

    @Enumerated (EnumType.STRING)
    private OrderStatus status; // "PENDING", "COMPLETED", "CANCELLED"

    // Quan hệ N-1: Nhiều Order thuộc về 1 User
    @ManyToOne
    @JoinColumn(name = "user_id") // Tạo cột user_id làm khóa ngoại trong bảng orders
    private User user;

    // Getter & Setter cho user
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Order() {}

    public Order(LocalDateTime orderDate, OrderStatus status) {
        this.orderDate = orderDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}