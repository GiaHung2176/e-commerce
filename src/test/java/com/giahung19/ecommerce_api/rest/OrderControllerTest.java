package com.giahung19.ecommerce_api.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.giahung19.ecommerce_api.security.CustomUserDetailsService;
import com.giahung19.ecommerce_api.security.JwtAccessDeniedHandler;
import com.giahung19.ecommerce_api.security.JwtAuthenticationEntryPoint;
import com.giahung19.ecommerce_api.security.JwtService;
import com.giahung19.ecommerce_api.security.SecurityConfig;
import com.giahung19.ecommerce_api.service.OrderService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.security.test.context.support.WithMockUser;
import com.giahung19.ecommerce_api.dto.OrderResponseDTO;
import com.giahung19.ecommerce_api.entity.OrderStatus;
import com.giahung19.ecommerce_api.exception.ResourceNotFoundException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest (OrderController.class)
@Import({SecurityConfig.class,JwtAuthenticationEntryPoint.class,JwtAccessDeniedHandler.class})
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
    

        // 🧪 Test case 1: chưa đăng nhập -> 401
    @Test
    void createOrder_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"items\":[{\"productId\":5,\"quantity\":1}]}"))
            .andExpect(status().isUnauthorized());
    }

    // 🧪 Test case 2: đã đăng nhập, body hợp lệ -> 201, username lấy từ người đăng nhập chứ không từ body
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void createOrder_WhenAuthenticatedAndBodyValid_ShouldReturn201() throws Exception {
        OrderResponseDTO createdOrder = new OrderResponseDTO();
        createdOrder.setId(1L);
        createdOrder.setStatus(OrderStatus.PENDING);
        createdOrder.setUsername("alice");
        when(orderService.createOrder(eq("alice"), any())).thenReturn(createdOrder);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":5,\"quantity\":1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    // 🧪 Test case 3: items rỗng -> 400, lỗi theo field
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void createOrder_WhenItemsEmpty_ShouldReturn400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("items"));
    }

    // 🧪 Test case 4: hai dòng cùng productId -> 400 (custom validation @NoDuplicateProducts)
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void createOrder_WhenItemsHaveDuplicateProductId_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":5,\"quantity\":1},{\"productId\":5,\"quantity\":2}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("items"));
    }

    // 🧪 Test case 5: CUSTOMER gọi API chỉ dành cho ADMIN -> 403
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void findAll_WhenUserIsCustomer_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    // 🧪 Test case 6: Service báo không tìm thấy sản phẩm -> 404
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void createOrder_WhenServiceThrowsResourceNotFound_ShouldReturn404() throws Exception {
        when(orderService.createOrder(eq("alice"), any()))
                .thenThrow(new ResourceNotFoundException("Not found product"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":99,\"quantity\":1}]}"))
                .andExpect(status().isNotFound());
    }

    // 🧪 Test case 7: xung đột khóa lạc quan (hai người đặt cùng lúc) -> 409
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void createOrder_WhenServiceThrowsConcurrencyFailure_ShouldReturn409() throws Exception {
        when(orderService.createOrder(eq("alice"), any()))
                .thenThrow(new ConcurrencyFailureException("version mismatch"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":5,\"quantity\":1}]}"))
                .andExpect(status().isConflict());
    }

}
