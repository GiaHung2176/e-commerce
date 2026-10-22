package com.giahung19.ecommerce_api.service;

import com.giahung19.ecommerce_api.dto.CreateOrderRequestDTO;
import com.giahung19.ecommerce_api.dto.OrderItemRequestDTO;
import com.giahung19.ecommerce_api.entity.Product;
import com.giahung19.ecommerce_api.entity.Role;
import com.giahung19.ecommerce_api.entity.User;
import com.giahung19.ecommerce_api.exception.BadRequestException;
import com.giahung19.ecommerce_api.repository.ProductRepository;
import com.giahung19.ecommerce_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Cố ý KHÔNG đặt @Transactional ở lớp test: mỗi luồng phải có transaction riêng và commit thật,
// nếu không hai luồng sẽ không giẫm lên nhau như hai request thật.
// Chạy trên DB thật (MySQL local), nên mỗi lượt tự dọn dữ liệu mình tạo ở @AfterEach.
@SpringBootTest
class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long productId;
    private final List<Long> userIds = new ArrayList<>();
    private String firstUsername;
    private String secondUsername;

    @BeforeEach
    void setUp() {
        long suffix = System.nanoTime();
        firstUsername = "concurrency_a_" + suffix;
        secondUsername = "concurrency_b_" + suffix;

        userIds.add(userRepository.save(new User(firstUsername, "not-a-real-password", Role.CUSTOMER)).getId());
        userIds.add(userRepository.save(new User(secondUsername, "not-a-real-password", Role.CUSTOMER)).getId());

        // Sản phẩm chỉ còn đúng 1 cái
        Product product = new Product("Concurrency test product " + suffix, new BigDecimal("100000"), 1);
        productId = productRepository.save(product).getId();
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("delete from order_items where product_id = ?", productId);
        for (Long userId : userIds) {
            jdbcTemplate.update("delete from orders where user_id = ?", userId);
        }
        jdbcTemplate.update("delete from product_tags where product_id = ?", productId);
        productRepository.deleteById(productId);
        for (Long userId : userIds) {
            userRepository.deleteById(userId);
        }
        userIds.clear();
    }

    // Lặp nhiều lần vì race condition không phải lần nào cũng lộ ra
    @RepeatedTest(10)
    void createOrder_WhenTwoUsersOrderLastItemAtTheSameTime_ShouldSellItOnlyOnce() throws Exception {
        CreateOrderRequestDTO requestDTO = new CreateOrderRequestDTO(
                List.of(new OrderItemRequestDTO(productId, 1)));

        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Boolean> firstOrder = () -> placeOrder(startSignal, firstUsername, requestDTO);
        Callable<Boolean> secondOrder = () -> placeOrder(startSignal, secondUsername, requestDTO);

        Future<Boolean> firstResult = executor.submit(firstOrder);
        Future<Boolean> secondResult = executor.submit(secondOrder);

        startSignal.countDown(); // cho cả hai luồng xuất phát cùng lúc
        int successCount = 0;
        if (firstResult.get()) {
            successCount++;
        }
        if (secondResult.get()) {
            successCount++;
        }
        executor.shutdown();

        Integer orderItemCount = jdbcTemplate.queryForObject(
                "select count(*) from order_items where product_id = ?", Integer.class, productId);
        Integer remainingStock = productRepository.findById(productId).orElseThrow().getStockQuantity();

        assertEquals(1, successCount, "Chỉ đúng 1 request được đặt thành công");
        assertEquals(1, orderItemCount, "Chỉ đúng 1 dòng order_items cho sản phẩm này");
        assertEquals(0, remainingStock, "Stock phải về 0, không được âm hay còn nguyên");
    }

    // true = đặt thành công, false = bị từ chối đúng cách: hết hàng (BadRequestException) hoặc xung đột
    // đồng thời (ConcurrencyFailureException: version lệch hoặc deadlock, GlobalExceptionHandler trả 409).
    // Bị từ chối vì lý do khác thì ném lại để test đỏ.
    private boolean placeOrder(CountDownLatch startSignal, String username, CreateOrderRequestDTO requestDTO)
            throws InterruptedException {
        startSignal.await();
        try {
            orderService.createOrder(username, requestDTO);
            return true;
        } catch (ConcurrencyFailureException | BadRequestException expectedRejection) {
            System.out.println("[" + username + "] rejected: " + expectedRejection.getClass().getSimpleName());
            return false;
        }
    }
}
