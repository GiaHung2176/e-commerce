package com.giahung19.ecommerce_api.dto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.giahung19.ecommerce_api.entity.OrderStatus;

public class OrderResponseDTO {

    private Long id;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private String username;
    private List<OrderItemResponseDTO> items = new ArrayList<>();

    public OrderResponseDTO() {}

    public OrderResponseDTO(Long id, LocalDateTime orderDate, OrderStatus status, String username, List<OrderItemResponseDTO> items) {
        this.id = id;
        this.orderDate = orderDate;
        this.status = status;
        this.username = username;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<OrderItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponseDTO> items) {
        this.items = items;
    }

}
