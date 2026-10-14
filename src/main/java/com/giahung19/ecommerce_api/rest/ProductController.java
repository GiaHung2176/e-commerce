package com.giahung19.ecommerce_api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.giahung19.ecommerce_api.dto.*;
import com.giahung19.ecommerce_api.service.ProductService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;
import org.springframework.data.domain.Page;
import com.giahung19.ecommerce_api.exception.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Validator;

@Tag(name = "Product", description = "Quản lý sản phẩm")
@RequestMapping ("/api/products")
@RestController
public class ProductController {
    
    private final ProductService productService;
    private final JsonMapper jsonMapper;
    private final Validator validator;

    @Autowired 
    public ProductController (ProductService productService,JsonMapper jsonMapper,Validator validator){
        this.jsonMapper=jsonMapper;
        this.productService=productService;
        this.validator=validator;
    }

    @Operation(summary = "Lấy danh sách sản phẩm, có phân trang và sắp xếp")
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> findAll(
        @RequestParam (defaultValue = "0") int page,
        @RequestParam (defaultValue = "10") int size,
        @RequestParam (defaultValue = "id") String sortBy,
        @RequestParam (defaultValue = "asc") String sortDir){
        
        Page<ProductResponseDTO> products =productService.findAll(page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Lấy chi tiết một sản phẩm theo id")
    @GetMapping ("/{id}")
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable Long id){
        ProductResponseDTO productResponseDTO =productService.findById(id);
        return ResponseEntity.ok(productResponseDTO);
    }

    @Operation(summary = "Tạo sản phẩm mới")
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {
        ProductResponseDTO createProduct =productService.save(requestDTO);
        return new ResponseEntity<>(createProduct,HttpStatus.CREATED);
    }

    @Operation(summary = "Cập nhật toàn bộ thông tin sản phẩm")
    @PutMapping ("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id,@Valid @RequestBody ProductRequestDTO requestDTO){
        ProductResponseDTO updateProduct =productService.update(id, requestDTO);
        return ResponseEntity.ok(updateProduct);
    }
    private void validateProductPatch(ProductRequestDTO requestDTO) {
        Set<ConstraintViolation<ProductRequestDTO>> violations = validator.validate(requestDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
    @Operation(summary = "Cập nhật một phần thông tin sản phẩm")
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> patchProduct(@PathVariable Long id,@RequestBody Map<String, Object> patchPayload) {
        
        if (patchPayload.containsKey("id")) {
            throw new BadRequestException("Product id not allowed in request body");
        }

        ProductResponseDTO currentDTO = productService.findById(id);
        ProductResponseDTO patchedDTO = jsonMapper.updateValue(currentDTO, patchPayload);
        ProductRequestDTO requestDTO = new ProductRequestDTO(patchedDTO.getCategoryId(),patchedDTO.getName(),patchedDTO.getPrice(),patchedDTO.getStockQuantity());
        validateProductPatch(requestDTO);
        ProductResponseDTO updatedProduct = productService.update(id, requestDTO);
        return ResponseEntity.ok(updatedProduct);
    }  

    @Operation(summary = "Xóa sản phẩm theo id")
    @DeleteMapping ("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.ok("Deleted product with id: " + id);
    }

}
