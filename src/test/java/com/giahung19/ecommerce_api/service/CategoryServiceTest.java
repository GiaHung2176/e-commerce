package com.giahung19.ecommerce_api.service;

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
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository; // Giả lập CategoryRepository

    @Mock
    private ProductRepository productRepository;   // Giả lập ProductRepository

    @InjectMocks
    private CategoryServiceImpl categoryService;   // Tiêm 2 Repository giả ở trên vào Service thật này

    // 🧪 Test case 1: delete not valid category -> throw ResourceNotFoundException
    @Test 
    public void deleteById_WhenCategoryNotFound_ShouldThrowResourceNotFoundException(){

        // arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // act & assert 
        assertThrows(ResourceNotFoundException.class,()-> {
            categoryService.deleteById(999L);
        });
    }


    // 🧪 Test case 2: delete category still has product -> throw DataConflictException
    @Test 
    public void deleteById_WhenCategoryHasProducts_ShouldThrowDataConflictException(){
        
        // arrange
        Long categoryId=1L;
        Category category=new Category();
        category.setId(categoryId);

        when (categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        when (productRepository.existsByCategoryId(categoryId)).thenReturn(true);

        // act & assert 
        assertThrows(DataConflictException.class,()->{
            categoryService.deleteById(categoryId);
        });

        // verify
        verify(categoryRepository,never()).delete(any());
    }


    // Test case 3: delete category successfully (category has no product)
    @Test 
    public void deleteById_WhenCategoryExistsAndHasNoProducts_ShouldDeleteSuccessfully(){

        // arrange 
        Long categoryId=1L;
        Category category =new Category();
        category.setId(categoryId);
        category.setName("Điện thoại");

        when (categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        when (productRepository.existsByCategoryId(categoryId)).thenReturn(false);

        // act & assert 
        assertDoesNotThrow(()->{
            categoryService.deleteById(categoryId);
        });

        // verify 
        verify(categoryRepository, times(1)).delete(category);
    }
}