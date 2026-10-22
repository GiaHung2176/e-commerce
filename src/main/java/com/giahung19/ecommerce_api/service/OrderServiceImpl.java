package com.giahung19.ecommerce_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import com.giahung19.ecommerce_api.entity.*;
import com.giahung19.ecommerce_api.exception.*;
import com.giahung19.ecommerce_api.repository.*;import jakarta.transaction.Transactional;
import com.giahung19.ecommerce_api.dto.CreateOrderRequestDTO;
import com.giahung19.ecommerce_api.dto.OrderItemRequestDTO;
import com.giahung19.ecommerce_api.dto.OrderItemResponseDTO;
import com.giahung19.ecommerce_api.dto.OrderResponseDTO;


@Service
public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,UserRepository userRepository,ProductRepository productRepository){
        this.orderRepository=orderRepository;
        this.productRepository=productRepository;
        this.userRepository =userRepository;
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

    @Override 
    @Transactional 
    public OrderResponseDTO createOrder(String username, CreateOrderRequestDTO requestDTO){
        User user =userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order =new Order(LocalDateTime.now(),OrderStatus.PENDING);
        order.setUser(user);

        for (OrderItemRequestDTO itemRequest :requestDTO.getItems()){
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Not found product"));
            
            if(product.getStockQuantity()<itemRequest.getQuantity()){
                throw new BadRequestException("Current stock is less than request quantity or out of stock");
            }

            product.setStockQuantity(product.getStockQuantity()-itemRequest.getQuantity());

            OrderItem orderItem =new OrderItem(itemRequest.getQuantity(),product.getPrice());
            orderItem.setProduct(product);

            order.addItem(orderItem);

        }
        Order savedOrder =orderRepository.save(order);  
        OrderResponseDTO orderResponseDTO =convertToResponseDTO(savedOrder);
        return orderResponseDTO;
    }

    @Override
    public List<OrderResponseDTO> findMyOrders(String username) {
        List<Order> orders=orderRepository.findAllByUsernameWithItems(username);
        return orders.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional 
    @Override
    public OrderResponseDTO cancelOrder(String username, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Not found order"));
        
        if(!order.getUser().getUsername().equals(username)) throw new AccessDeniedException("Not allowed to access");
        if(order.getStatus()!=OrderStatus.PENDING) throw new BadRequestException("Not allow this operation");

        order.setStatus(OrderStatus.CANCELLED);

        return convertToResponseDTO(order);
    }


}
