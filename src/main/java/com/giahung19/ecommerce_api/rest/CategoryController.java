package com.giahung19.ecommerce_api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.giahung19.ecommerce_api.service.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;
import com.giahung19.ecommerce_api.dto.CategoryRequestDTO;
import com.giahung19.ecommerce_api.dto.CategoryResponseDTO;
import com.giahung19.ecommerce_api.exception.BadRequestException;
import jakarta.validation.Validation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController 
@RequestMapping ("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final JsonMapper jsonMapper;

    @Autowired 
    public CategoryController (CategoryService categoryService,JsonMapper jsonMapper){
        this.categoryService=categoryService;
        this.jsonMapper=jsonMapper;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> findAll(){
        List<CategoryResponseDTO> list=categoryService.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping ("/{id}")
    public ResponseEntity<CategoryResponseDTO> findById(@PathVariable Long id){
        CategoryResponseDTO categoryResponseDTO =categoryService.findById(id);
        return ResponseEntity.ok(categoryResponseDTO);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        CategoryResponseDTO createCategory =categoryService.save(requestDTO);
        return new ResponseEntity<>(createCategory,HttpStatus.CREATED);
    }

    @PutMapping ("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id,@Valid @RequestBody CategoryRequestDTO requestDTO){
        CategoryResponseDTO updateCategory =categoryService.update(id, requestDTO);
        return ResponseEntity.ok(updateCategory);
    }
    private void validateCategoryPatch(CategoryRequestDTO requestDTO) {
        Set<ConstraintViolation<CategoryRequestDTO>> violations = Validation
            .buildDefaultValidatorFactory()
            .getValidator()
            .validate(requestDTO);

        if (!violations.isEmpty()) {
            String errorMessage = violations.iterator().next().getMessage();
            throw new BadRequestException(errorMessage);
        }
}
    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> patchCategory(@PathVariable Long id,@RequestBody Map<String, Object> patchPayload) {
        
        if (patchPayload.containsKey("id")) {
            throw new BadRequestException("Category id not allowed in request body");
        }

        CategoryResponseDTO currentDTO = categoryService.findById(id);
        CategoryResponseDTO patchedDTO = jsonMapper.updateValue(currentDTO, patchPayload);
        CategoryRequestDTO requestDTO = new CategoryRequestDTO(patchedDTO.getName(), patchedDTO.getDescription());
        validateCategoryPatch(requestDTO);
        CategoryResponseDTO updatedCategory = categoryService.update(id, requestDTO);
        return ResponseEntity.ok(updatedCategory);
    }   

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteById(id);
        return ResponseEntity.ok("Deleted category with id: " + id);
    }

}
