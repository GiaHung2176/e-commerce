package com.giahung19.ecommerce_api.service;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.giahung19.ecommerce_api.entity.*;
import com.giahung19.ecommerce_api.exception.*;
import com.giahung19.ecommerce_api.repository.*;
import com.giahung19.ecommerce_api.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;



@Service 
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;


    @Autowired 
    public ProductServiceImpl(ProductRepository productRepository,CategoryRepository categoryRepository){
        this.productRepository=productRepository;
        this.categoryRepository=categoryRepository;
    } 

    private ProductResponseDTO convertToResponseDTO(Product product){
        ProductResponseDTO dto =new ProductResponseDTO();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }
        return dto;
    }

    private static final Set<String> SORT_FIELDS =Set.of("id","name","price","stockQuantity");

    public Page<ProductResponseDTO> findAll(int page,int size,String sortBy,String sortDir){
        String nomalizedSortBy =(sortBy==null||sortBy.isBlank())?"id":sortBy.trim();
        String nomalizedSortDir=(sortDir==null||sortDir.isBlank()) ? "asc":sortDir.trim();

        if(!SORT_FIELDS.contains(nomalizedSortBy)){
            throw new BadRequestException("Invalid sort field: "+ nomalizedSortBy);
        }
        if (!nomalizedSortDir.equalsIgnoreCase("asc") && !nomalizedSortDir.equalsIgnoreCase("desc")) {
            throw new BadRequestException("Invalid sort direction: '" + nomalizedSortDir + "'. Must be 'asc' or 'desc'");
        }

        if (page < 0) {
            page = 0;
        }
        if (size > 50) {
            size = 50;
        }
        if (size <= 0) {
            size = 10; 
        }
        Sort sort = nomalizedSortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
            ? Sort.by(nomalizedSortBy).ascending() 
            : Sort.by(nomalizedSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(this::convertToResponseDTO);
    }

    public ProductResponseDTO findById(Long id){
        Product product= productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not found product with id: "+id));
        return convertToResponseDTO(product);
    }

    public ProductResponseDTO save(ProductRequestDTO requestDTO){
        Category category =categoryRepository.findById(requestDTO.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Not found category with id: "+requestDTO.getCategoryId()));
        
        Product product=new Product();
        product.setName(requestDTO.getName());
        product.setPrice(requestDTO.getPrice());
        product.setStockQuantity(requestDTO.getStockQuantity());
        product.setCategory(category);
        
        Product savedDB=productRepository.save(product);

        return convertToResponseDTO(savedDB);
    }

    public ProductResponseDTO update(Long id, ProductRequestDTO requestDTO){
        Product product= productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not found product with id: "+id));
        
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + requestDTO.getCategoryId()));
            
        product.setName(requestDTO.getName());
        product.setPrice(requestDTO.getPrice());
        product.setStockQuantity(requestDTO.getStockQuantity());
        product.setCategory(category);

        Product savedDB=productRepository.save(product);
        return convertToResponseDTO(savedDB);
    }

    public void deleteById(Long id){
        Product product =productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not found product with id: "+id));
        productRepository.delete(product);
    }

}
