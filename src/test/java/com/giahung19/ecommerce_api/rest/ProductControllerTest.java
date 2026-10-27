package com.giahung19.ecommerce_api.rest;

import com.giahung19.ecommerce_api.dto.ProductResponseDTO;
import com.giahung19.ecommerce_api.exception.ResourceNotFoundException;
import com.giahung19.ecommerce_api.security.CustomUserDetailsService;
import com.giahung19.ecommerce_api.security.JwtAccessDeniedHandler;
import com.giahung19.ecommerce_api.security.JwtAuthenticationEntryPoint;
import com.giahung19.ecommerce_api.security.JwtService;
import com.giahung19.ecommerce_api.security.SecurityConfig;
import com.giahung19.ecommerce_api.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // 🧪 Test case 1: chưa đăng nhập -> 401
    @Test
    void findAll_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    // 🧪 Test case 2: CUSTOMER đã đăng nhập -> 200, JSON có content
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void findAll_WhenAuthenticated_ShouldReturn200WithContent() throws Exception {
        ProductResponseDTO laptop = new ProductResponseDTO(1L, 2L, "Laptop", new BigDecimal("1500.00"), 10);
        when(productService.findAll(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of(laptop)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"));
    }

    // 🧪 Test case 3: id không tồn tại -> 404
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void findById_WhenProductNotFound_ShouldReturn404() throws Exception {
        when(productService.findById(99L)).thenThrow(new ResourceNotFoundException("Product not found with id: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    // 🧪 Test case 4: CUSTOMER tạo sản phẩm -> 403
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void createProduct_WhenUserIsCustomer_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"categoryId\":2,\"name\":\"Laptop\",\"price\":1500.00,\"stockQuantity\":10}"))
        .andExpect(status().isForbidden());

        verify(productService, never()).save(any());
    }

    // 🧪 Test case 5: ADMIN, body hợp lệ -> 201
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createProduct_WhenAdminAndBodyValid_ShouldReturn201() throws Exception {
        ProductResponseDTO created = new ProductResponseDTO(1L, 2L, "Laptop", new BigDecimal("1500.00"), 10);
        when(productService.save(any())).thenReturn(created);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":2,\"name\":\"Laptop\",\"price\":1500.00,\"stockQuantity\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    // 🧪 Test case 6: ADMIN, name rỗng -> 400, lỗi theo field name
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createProduct_WhenNameBlank_ShouldReturn400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":2,\"name\":\"\",\"price\":1500.00,\"stockQuantity\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    // 🧪 Test case 7: ADMIN, PATCH có "id" trong body -> 400
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void patchProduct_WhenBodyContainsId_ShouldReturn400() throws Exception {
        mockMvc.perform(patch("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1\"name\":\"Laptop\",\"price\":1500.00,\"stockQuantity\":10}"))
                .andExpect(status().isBadRequest());
    }

    // 🧪 Test case 8: ADMIN xóa sản phẩm -> 200
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteProduct_WhenAdmin_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk());
    }
}
