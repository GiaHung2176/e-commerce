package com.giahung19.ecommerce_api.rest;

import com.giahung19.ecommerce_api.dto.CategoryResponseDTO;
import com.giahung19.ecommerce_api.exception.ResourceNotFoundException;
import com.giahung19.ecommerce_api.security.CustomUserDetailsService;
import com.giahung19.ecommerce_api.security.JwtAccessDeniedHandler;
import com.giahung19.ecommerce_api.security.JwtAuthenticationEntryPoint;
import com.giahung19.ecommerce_api.security.JwtService;
import com.giahung19.ecommerce_api.security.SecurityConfig;
import com.giahung19.ecommerce_api.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // 🧪 Test case 1: CUSTOMER đã đăng nhập -> 200, JSON là mảng
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void findAll_WhenAuthenticated_ShouldReturn200WithList() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(new CategoryResponseDTO(1L, "Laptop", "Máy tính xách tay")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    // 🧪 Test case 2: id không tồn tại -> 404
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void findById_WhenCategoryNotFound_ShouldReturn404() throws Exception {
        when(categoryService.findById(99L)).thenThrow(new ResourceNotFoundException("Category not found with id: 99"));

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound());
    }

    // 🧪 Test case 3: CUSTOMER tạo danh mục -> 403
    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void createCategory_WhenUserIsCustomer_ShouldReturn403() throws Exception {
        // TODO(human): POST /api/categories với body hợp lệ bằng user CUSTOMER, expect 403,
        //  rồi verify(categoryService, never()).save(any()). Giống test 4 của ProductControllerTest.
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop\",\"description\":\"Máy tính xách tay\"}"))
                .andExpect(status().isForbidden());
        verify(categoryService, never()).save(any());
    }

    // 🧪 Test case 4: ADMIN, body hợp lệ -> 201
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCategory_WhenAdminAndBodyValid_ShouldReturn201() throws Exception {
        when(categoryService.save(any())).thenReturn(new CategoryResponseDTO(1L, "Laptop", "Máy tính xách tay"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop\",\"description\":\"Máy tính xách tay\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    // 🧪 Test case 5: ADMIN, name rỗng -> 400, lỗi theo field name
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCategory_WhenNameBlank_ShouldReturn400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"Máy tính xách tay\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    // 🧪 Test case 6: ADMIN, PATCH hợp lệ -> 200 (controller gộp payload vào DTO hiện tại bằng jsonMapper)
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void patchCategory_WhenPayloadValid_ShouldReturn200() throws Exception {
        when(categoryService.findById(1L)).thenReturn(new CategoryResponseDTO(1L, "Laptop", "Mô tả cũ"));
        when(categoryService.update(eq(1L), any())).thenReturn(new CategoryResponseDTO(1L, "Laptop", "Mô tả mới"));

        mockMvc.perform(patch("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Mô tả mới\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Mô tả mới"));
    }

    // 🧪 Test case 7: ADMIN, PATCH với name rỗng -> 400, lỗi theo field name
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void patchCategory_WhenNameBlank_ShouldReturn400WithFieldError() throws Exception {
        // TODO(human): when(categoryService.findById(1L)) trả CategoryResponseDTO hiện tại, rồi
        //  PATCH /api/categories/1 với {"name":""}, expect 400 và $.errors[0].field = "name".
        //  Vì sao: controller gộp payload, validate thủ công, ném ConstraintViolationException.
        //  Nếu không mock findById thì currentDTO là null, test sẽ ra lỗi khác.
        CategoryResponseDTO categoryResponseDTO=new CategoryResponseDTO(1L, "Laptop", "a brand new laptop");
        when (categoryService.findById(1L)).thenReturn(categoryResponseDTO);

        mockMvc.perform(patch("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"Máy tính xách tay\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    // 🧪 Test case 8: ADMIN xóa danh mục -> 200
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteCategory_WhenAdmin_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk());
    }
}
