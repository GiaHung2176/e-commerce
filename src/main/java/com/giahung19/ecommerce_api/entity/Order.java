package com.giahung19.ecommerce_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // TODO(human): quan hệ 1-N tới OrderItem: List<OrderItem> items, khởi tạo = new ArrayList<>().
    //              Cần: mappedBy trỏ tới field nào của OrderItem? cascade loại nào? orphanRemoval có cần không, vì sao?
    @OneToMany (mappedBy = "order",cascade = {CascadeType.PERSIST,CascadeType.REMOVE},orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
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

    public List<OrderItem> getItems(){
        return this.items;
    }
    public void setItems(List<OrderItem> items){
        this.items=items;
    }

    public boolean addItem(OrderItem orderItem){
        if(orderItem!=null){
            items.add(orderItem);
            orderItem.setOrder(this);
            return true;
        }
        return false;
    }

    public boolean removeItem(OrderItem orderItem){
        if(orderItem!=null && items.remove(orderItem)){
            orderItem.setOrder(null);
            return true;
        }
        return false;
    }
}