package com.giahung19.ecommerce_api.repository;

import com.giahung19.ecommerce_api.entity.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import com.giahung19.ecommerce_api.support.MySqlTestContainerConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Không đặt @Transactional ở lớp test: mỗi lần gọi repository là một transaction riêng,
// nên hai lần findById trả về hai bản Product tách rời nhau, giống hai request khác nhau.
@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig.class)
class ProductOptimisticLockingTest {

    @Autowired
    private ProductRepository productRepository;

    private Long productId;

    @AfterEach
    void cleanUp() {
        productRepository.deleteById(productId);
    }

    @Test
    void save_WhenProductWasChangedByAnotherRequest_ShouldThrowOptimisticLockingFailure() {
        productId = productRepository.save(new Product("Optimistic lock test product", new BigDecimal("100000"), 5)).getId();

        // Hai "request" cùng đọc, cùng thấy version cũ
        Product firstRequestCopy = productRepository.findById(productId).orElseThrow();
        Product secondRequestCopy = productRepository.findById(productId).orElseThrow();

        // Request thứ nhất ghi trước, version tăng lên
        firstRequestCopy.setStockQuantity(4);
        productRepository.save(firstRequestCopy);

        // Request thứ hai vẫn cầm version cũ nên phải bị từ chối
        secondRequestCopy.setStockQuantity(3);
        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> productRepository.save(secondRequestCopy));

        // Thay đổi của request thứ hai không được ghi đè lên request thứ nhất
        Product storedProduct = productRepository.findById(productId).orElseThrow();
        assertEquals(4, storedProduct.getStockQuantity());
        assertEquals(1L, storedProduct.getVersion());
    }
}
