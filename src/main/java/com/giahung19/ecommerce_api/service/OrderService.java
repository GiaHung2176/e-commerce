package com.giahung19.ecommerce_api.service;

import com.giahung19.ecommerce_api.dto.CreateOrderRequestDTO;
import com.giahung19.ecommerce_api.dto.OrderResponseDTO;
import java.util.List;

public interface OrderService {
    List<OrderResponseDTO> findAll();

    OrderResponseDTO createOrder(String username, CreateOrderRequestDTO requestDTO);

    List<OrderResponseDTO> findMyOrders(String username);

    OrderResponseDTO cancelOrder(String username, Long orderId);
}
