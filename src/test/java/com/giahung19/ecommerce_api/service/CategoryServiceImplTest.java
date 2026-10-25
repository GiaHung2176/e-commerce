package com.giahung19.ecommerce_api.service;

import com.giahung19.ecommerce_api.dto.CategoryRequestDTO;
import com.giahung19.ecommerce_api.dto.CategoryResponseDTO;
import com.giahung19.ecommerce_api.entity.Category;
import com.giahung19.ecommerce_api.exception.DataConflictException;
import com.giahung19.ecommerce_api.exception.ResourceNotFoundException;
import com.giahung19.ecommerce_api.repository.CategoryRepository;
import com.giahung19.ecommerce_api.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category buildCategory(Long id, String name, String description) {
        Category category = new Category(name, description);
        category.setId(id);
        return category;
    }

    // 🧪 Test case 1: có 2 category -> trả list DTO đúng số lượng và dữ liệu
    @Test
    void findAll_WhenCategoriesExist_ShouldReturnListOfResponseDTO() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                buildCategory(1L, "Laptop", "Laptop category"),
                buildCategory(2L, "Phone", "Phone category")));

        List<CategoryResponseDTO> result = categoryService.findAll();

        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals("Phone", result.get(1).getName());
    }

    // 🧪 Test case 2: tìm thấy category -> trả DTO đúng dữ liệu
    @Test
    void findById_WhenCategoryExists_ShouldReturnResponseDTO() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(buildCategory(1L, "Laptop", "Laptop category")));

        CategoryResponseDTO result = categoryService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals("Laptop category", result.getDescription());
    }

    // 🧪 Test case 3: không có category -> 404
    @Test
    void findById_WhenCategoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,()->categoryService.findById(1L));
    }

    // 🧪 Test case 4: tên đã tồn tại -> 409, không lưu
    @Test
    void save_WhenNameAlreadyExists_ShouldThrowDataConflictException() {
        CategoryRequestDTO requestDTO = new CategoryRequestDTO("Laptop", "Laptop category");
        when(categoryRepository.existsByName("Laptop")).thenReturn(true);

        assertThrows(DataConflictException.class, () -> categoryService.save(requestDTO));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // 🧪 Test case 5: dữ liệu hợp lệ -> lưu và trả DTO
    @Test
    void save_WhenNameIsNew_ShouldSaveAndReturnResponseDTO() {
        CategoryRequestDTO requestDTO = new CategoryRequestDTO("Laptop", "Laptop category");
        when(categoryRepository.existsByName("Laptop")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category savedCategory = invocation.getArgument(0);
            savedCategory.setId(1L);
            return savedCategory;
        });

        CategoryResponseDTO result = categoryService.save(requestDTO);

        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals("Laptop category", result.getDescription());
    }

    // 🧪 Test case 6: cập nhật category không tồn tại -> 404, không lưu
    @Test
    void update_WhenCategoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        CategoryRequestDTO requestDTO = new CategoryRequestDTO("Laptop", "Laptop category");
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.update(99L, requestDTO));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // 🧪 Test case 7: đổi sang tên đã thuộc category khác -> 409, không lưu
    @Test
    void update_WhenNewNameBelongsToAnotherCategory_ShouldThrowDataConflictException() {
        Category existingCategory = buildCategory(1L, "Laptop", "Laptop category");
        CategoryRequestDTO requestDTO = new CategoryRequestDTO("Phone", "Phone category");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByName("Phone")).thenReturn(true);

        assertThrows(DataConflictException.class, () -> categoryService.update(1L, requestDTO));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // 🧪 Test case 8: giữ nguyên tên, chỉ đổi mô tả -> không kiểm tra trùng tên, lưu thành công
    @Test
    void update_WhenNameIsUnchanged_ShouldNotCheckNameAndShouldSave() {
        Category existingCategory = buildCategory(1L, "Laptop", "Old description");
        CategoryRequestDTO requestDTO = new CategoryRequestDTO("Laptop", "New description");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponseDTO result = categoryService.update(1L, requestDTO);

        assertEquals("New description", result.getDescription());
        verify(categoryRepository, never()).existsByName(any());
    }

    // 🧪 Test case 9: xóa category không tồn tại -> 404, không gọi delete
    @Test
    void deleteById_WhenCategoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteById(99L));

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    // 🧪 Test case 10: category còn sản phẩm -> 409, không xóa
    @Test
    void deleteById_WhenCategoryStillHasProducts_ShouldThrowDataConflictException() {
        Category category =new Category("Phone", "a mobile phone");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(DataConflictException.class,() ->categoryService.deleteById(1L));
        verify(categoryRepository, never()).delete(any(Category.class));

    }
}
