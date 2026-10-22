package com.giahung19.ecommerce_api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.giahung19.ecommerce_api.dto.CreateOrderRequestDTO;
import com.giahung19.ecommerce_api.dto.OrderResponseDTO;
import jakarta.validation.Valid;
import com.giahung19.ecommerce_api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Order", description = "Quản lý đơn hàng")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService){
        this.orderService=orderService;
    }

    @Operation(summary = "Lấy danh sách tất cả đơn hàng")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> findAll(){
        List<OrderResponseDTO> list=orderService.findAll();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Tạo đơn hàng mới cho người dùng đang đăng nhập")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody CreateOrderRequestDTO requestDTO,
            Authentication authentication) {
        OrderResponseDTO createdOrder = orderService.createOrder(authentication.getName(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }
}
