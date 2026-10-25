package com.giahung19.ecommerce_api.service;

import com.giahung19.ecommerce_api.dto.ProductRequestDTO;
import com.giahung19.ecommerce_api.dto.ProductResponseDTO;
import com.giahung19.ecommerce_api.entity.Category;
import com.giahung19.ecommerce_api.entity.Product;
import com.giahung19.ecommerce_api.exception.BadRequestException;
import com.giahung19.ecommerce_api.exception.ResourceNotFoundException;
import com.giahung19.ecommerce_api.repository.CategoryRepository;
import com.giahung19.ecommerce_api.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category buildCategory(Long id) {
        Category category = new Category("Laptop", "Laptop category");
        category.setId(id);
        return category;
    }

    private Product buildProduct(Long id, Category category) {
        Product product = new Product("Dell XPS", new BigDecimal("30000000"), 5);
        product.setId(id);
        product.setCategory(category);
        return product;
    }

    // 🧪 Test case 1: sortBy không nằm trong danh sách cho phép -> 400
    @Test
    void findAll_WhenSortFieldIsInvalid_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class,
                () -> productService.findAll(0, 10, "password", "asc"));

        verify(productRepository, never()).findAll(any(Pageable.class));
    }

    // 🧪 Test case 2: sortDir khác asc/desc -> 400
    @Test
    void findAll_WhenSortDirectionIsInvalid_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class,
                () -> productService.findAll(0, 10, "name", "sideways"));

        verify(productRepository, never()).findAll(any(Pageable.class));
    }

    // 🧪 Test case 3: sortBy null và sortDir rỗng -> mặc định sắp xếp theo id tăng dần
    @Test
    void findAll_WhenSortParamsAreMissing_ShouldDefaultToIdAscending() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        productService.findAll(0, 10, null, "");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(pageableCaptor.capture());
        Sort.Order idOrder = pageableCaptor.getValue().getSort().getOrderFor("id");
        assertEquals(Sort.Direction.ASC, idOrder.getDirection());
    }

    // 🧪 Test case 4: size > 50 -> bị cắt về 50
    @Test
    void findAll_WhenSizeIsOver50_ShouldCapSizeAt50() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        productService.findAll(0, 100, "name", "desc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(pageableCaptor.capture());
        assertEquals(50, pageableCaptor.getValue().getPageSize());
    }

    // 🧪 Test case 5: page âm và size <= 0 -> về page 0, size 10
    @Test
    void findAll_WhenPageIsNegativeAndSizeIsZero_ShouldFallBackToPage0Size10() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        productService.findAll(-1, 0, "id", "asc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    // 🧪 Test case 6: tìm thấy sản phẩm -> trả DTO đúng dữ liệu
    @Test
    void findById_WhenProductExists_ShouldReturnResponseDTO() {
        Product product = buildProduct(1L, buildCategory(3L));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponseDTO result = productService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Dell XPS", result.getName());
        assertEquals(3L, result.getCategoryId());
    }

    // 🧪 Test case 7: không có sản phẩm -> 404
    @Test
    void findById_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,() ->productService.findById(1L));
    }

    // 🧪 Test case 8: category không tồn tại -> 404, không lưu sản phẩm
    @Test
    void save_WhenCategoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        ProductRequestDTO requestDTO = new ProductRequestDTO(99L, "Dell XPS", new BigDecimal("30000000"), 5);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.save(requestDTO));

        verify(productRepository, never()).save(any(Product.class));
    }

    // 🧪 Test case 9: dữ liệu hợp lệ -> lưu và trả DTO có categoryId
    @Test
    void save_WhenRequestIsValid_ShouldSaveAndReturnResponseDTO() {
        Category category = buildCategory(3L);
        ProductRequestDTO requestDTO = new ProductRequestDTO(3L, "Dell XPS", new BigDecimal("30000000"), 5);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(1L);
            return savedProduct;
        });

        ProductResponseDTO result = productService.save(requestDTO);

        assertEquals(1L, result.getId());
        assertEquals("Dell XPS", result.getName());
        assertEquals(3L, result.getCategoryId());
        assertEquals(5, result.getStockQuantity());
    }

    // 🧪 Test case 10: cập nhật sản phẩm không tồn tại -> 404, không lưu
    @Test
    void update_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        ProductRequestDTO requestDTO = new ProductRequestDTO(3L, "Dell XPS", new BigDecimal("30000000"), 5);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.update(99L, requestDTO));

        verify(productRepository, never()).save(any(Product.class));
    }

    
    @Test
    void deleteById_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,() ->productService.deleteById(1L));
        verify(productRepository, never()).delete(any(Product.class));
    }
}
