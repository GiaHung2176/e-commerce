package com.giahung19.ecommerce_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import com.giahung19.ecommerce_api.entity.*;
import com.giahung19.ecommerce_api.repository.*;
import com.giahung19.ecommerce_api.dto.OrderItemResponseDTO;
import com.giahung19.ecommerce_api.dto.OrderResponseDTO;

@Service
public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository){
        this.orderRepository=orderRepository;
    }

    private OrderItemResponseDTO convertToItemResponseDTO(OrderItem orderItem){
        OrderItemResponseDTO dto=new OrderItemResponseDTO();
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPriceAtOrder(orderItem.getPriceAtOrder());
        return dto;
    }

    private OrderResponseDTO convertToResponseDTO(Order order){
        OrderResponseDTO dto=new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setUsername(order.getUser().getUsername());
        for (OrderItem orderItem : order.getItems()){
            dto.getItems().add(convertToItemResponseDTO(orderItem));
        }
        return dto;
    }

    // find all
    @Override
    public List<OrderResponseDTO> findAll() {
        List<Order> orders =orderRepository.findAllWithUserAndItems();
        return orders.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }
}
