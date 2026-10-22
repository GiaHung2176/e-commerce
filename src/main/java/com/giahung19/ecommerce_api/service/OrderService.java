package com.giahung19.ecommerce_api.service;

import com.giahung19.ecommerce_api.dto.OrderResponseDTO;
import java.util.List;

public interface OrderService {
    List<OrderResponseDTO> findAll();
}
