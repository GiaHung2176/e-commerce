package com.giahung19.ecommerce_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import com.giahung19.ecommerce_api.entity.*;
import com.giahung19.ecommerce_api.exception.*;
import com.giahung19.ecommerce_api.repository.*;
import com.giahung19.ecommerce_api.dto.CategoryRequestDTO;
import com.giahung19.ecommerce_api.dto.CategoryResponseDTO;




@Service 
public class CategoryServiceImpl implements CategoryService {

    private CategoryRepository categoryRepository;
    private ProductRepository productRepository;

    @Autowired 
    public CategoryServiceImpl(CategoryRepository categoryRepository,ProductRepository productRepository){
        this.categoryRepository=categoryRepository;
        this.productRepository=productRepository;
    }

    private CategoryResponseDTO convertToResponseDTO(Category category){
        CategoryResponseDTO dto =new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

    // CRUD 

    // find all
    @Override
    public List<CategoryResponseDTO> findAll() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }
 

    // find 
    public CategoryResponseDTO findById(Long id){
        Category category= categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not found category with id: " + id));
        return convertToResponseDTO(category);
    }
    
    // save 
    public CategoryResponseDTO save(CategoryRequestDTO requestDTO){
        Category category=new Category();
        if (categoryRepository.existsByName(requestDTO.getName())) {
            throw new DataConflictException("Category name '" + requestDTO.getName() + "' already exists!");
        }
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        
        Category savedDB=categoryRepository.save(category);

        return convertToResponseDTO(savedDB);
    }

    // update 
    public CategoryResponseDTO update(Long id, CategoryRequestDTO requestDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found category with id: " + id));
        String currentName=category.getName();
        String newName=requestDTO.getName();
        if(!Objects.equals(newName,currentName)&&categoryRepository.existsByName(newName)){
            throw new DataConflictException("Category name '" + newName + "' already exists!");
        }
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return convertToResponseDTO(updatedCategory);
    }

    // delete 
    public void deleteById(Long id){
        Category category =categoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Not found category with id: "+id));
        boolean hasProducts =productRepository.existsByCategoryId(id);
        if(hasProducts){
            throw new DataConflictException("Cannot delete category with id " + id + " because it still contains products!");
        }
        categoryRepository.delete(category);
    }
}
