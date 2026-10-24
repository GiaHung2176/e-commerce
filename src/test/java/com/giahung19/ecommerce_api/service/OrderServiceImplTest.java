package com.giahung19.ecommerce_api.service;

import com.giahung19.ecommerce_api.dto.CreateOrderRequestDTO;
import com.giahung19.ecommerce_api.dto.OrderItemRequestDTO;
import com.giahung19.ecommerce_api.dto.OrderResponseDTO;
import com.giahung19.ecommerce_api.entity.Order;
import com.giahung19.ecommerce_api.entity.OrderStatus;
import com.giahung19.ecommerce_api.entity.Product;
import com.giahung19.ecommerce_api.entity.Role;
import com.giahung19.ecommerce_api.entity.User;
import com.giahung19.ecommerce_api.exception.BadRequestException;
import com.giahung19.ecommerce_api.exception.ResourceNotFoundException;
import com.giahung19.ecommerce_api.repository.OrderRepository;
import com.giahung19.ecommerce_api.repository.ProductRepository;
import com.giahung19.ecommerce_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order buildOrderOwnedBy(String username, OrderStatus status) {
        Order order = new Order(LocalDateTime.now(), status);
        order.setId(10L);
        order.setUser(new User(username, "not-a-real-password", Role.CUSTOMER));
        return order;
    }

    // 🧪 Test case 1: chủ đơn hủy đơn PENDING của mình -> thành CANCELLED
    @Test
    void cancelOrder_WhenOrderBelongsToUserAndIsPending_ShouldSetStatusCancelled() {
        Order order = buildOrderOwnedBy("alice", OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        OrderResponseDTO result = orderService.cancelOrder("alice", 10L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    // 🧪 Test case 2: người khác hủy đơn không phải của mình -> 403, đơn không bị đổi
    @Test
    void cancelOrder_WhenOrderBelongsToAnotherUser_ShouldThrowAccessDeniedException() {
        Order order = buildOrderOwnedBy("alice", OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> orderService.cancelOrder("bob", 10L));

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    // 🧪 Test case 3: đơn không tồn tại -> 404
    @Test
    void cancelOrder_WhenOrderNotFound_ShouldThrowResourceNotFoundException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder("alice", 999L));
    }

    // 🧪 Test case 4: đơn của mình nhưng đã COMPLETED -> 400, trạng thái giữ nguyên
    @Test
    void cancelOrder_WhenOrderIsNotPending_ShouldThrowBadRequestException() {
        Order order = buildOrderOwnedBy("alice", OrderStatus.COMPLETED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.cancelOrder("alice", 10L));

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    // 🧪 Test case 5: đơn của người khác và đã COMPLETED -> vẫn phải 403 (kiểm chủ đơn trước kiểm trạng thái),
    // nếu trả 400 thì người lạ dò ra được trạng thái đơn của người khác
    @Test
    void cancelOrder_WhenOrderOfAnotherUserIsNotPending_ShouldThrowAccessDeniedException() {
        Order order = buildOrderOwnedBy("alice", OrderStatus.COMPLETED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> orderService.cancelOrder("bob", 10L));
    }

    // 🧪 Test case 6: findMyOrders chỉ trả đơn mà repository lọc theo username, convert sang DTO
    @Test
    void findMyOrders_ShouldReturnOrdersOfThatUsernameAsDtos() {
        Order firstOrder = buildOrderOwnedBy("alice", OrderStatus.PENDING);
        Order secondOrder = buildOrderOwnedBy("alice", OrderStatus.COMPLETED);
        when(orderRepository.findAllByUsernameWithItems("alice")).thenReturn(List.of(firstOrder, secondOrder));

        List<OrderResponseDTO> result = orderService.findMyOrders("alice");

        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).getUsername());
        assertEquals("alice", result.get(1).getUsername());
    }

    // 🧪 Test case 7: kho không đủ -> 400, kho giữ nguyên, đơn không được lưu
    @Test
    void createOrder_WhenStockIsNotEnough_ShouldThrowBadRequestException() {
        // Arrange
        User user =new User("Camia","not-a-real-password",Role.CUSTOMER);
        when(userRepository.findByUsername("Camia")).thenReturn(Optional.of(user));

        Product product =new Product("Laptop", new BigDecimal("1000"), 1);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        CreateOrderRequestDTO requestDTO =new CreateOrderRequestDTO(List.of(new OrderItemRequestDTO(5L,3)));

        assertThrows(BadRequestException.class,() -> orderService.createOrder("Camia", requestDTO));
        assertEquals(1, product.getStockQuantity());   
        verify(orderRepository,never()).save(any());

    }

    // 🧪 Test case 8: đủ kho -> đơn PENDING, trừ đúng stock, lưu giá tại thời điểm đặt
    @Test
    void createOrder_WhenStockIsEnough_ShouldReduceStockAndSaveOrderAsPending() {
        User user = new User("alice", "not-a-real-password", Role.CUSTOMER);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Product product = new Product("Laptop", new BigDecimal("1000"), 5);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        // mock mặc định trả null cho save, convertToResponseDTO sẽ lỗi; nên cho save trả lại chính đơn được truyền vào
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequestDTO requestDTO = new CreateOrderRequestDTO(List.of(new OrderItemRequestDTO(5L, 3)));

        OrderResponseDTO result = orderService.createOrder("alice", requestDTO);

        assertEquals(2, product.getStockQuantity());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals("alice", result.getUsername());
        assertEquals(1, result.getItems().size());
        assertEquals(new BigDecimal("1000"), result.getItems().get(0).getPriceAtOrder());
        verify(orderRepository).save(any(Order.class));
    }

    // 🧪 Test case 9: user không tồn tại -> 404, không lưu đơn
    @Test
    void createOrder_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        CreateOrderRequestDTO requestDTO = new CreateOrderRequestDTO(List.of(new OrderItemRequestDTO(5L, 1)));

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder("ghost", requestDTO));
        verify(orderRepository, never()).save(any());
    }

    // 🧪 Test case 10: product không tồn tại -> 404, không lưu đơn
    @Test
    void createOrder_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        User user = new User("alice", "not-a-real-password", Role.CUSTOMER);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        CreateOrderRequestDTO requestDTO = new CreateOrderRequestDTO(List.of(new OrderItemRequestDTO(99L, 1)));

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder("alice", requestDTO));
        verify(orderRepository, never()).save(any());
    }

}
