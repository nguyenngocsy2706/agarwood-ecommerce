package com.sybanh.demo_website_tramhuong.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sybanh.demo_website_tramhuong.dto.request.CheckoutRequest;
import com.sybanh.demo_website_tramhuong.dto.request.OrderStatusUpdateRequest;
import com.sybanh.demo_website_tramhuong.dto.response.OrderResponse;
import com.sybanh.demo_website_tramhuong.entity.Cart;
import com.sybanh.demo_website_tramhuong.entity.CartItem;
import com.sybanh.demo_website_tramhuong.entity.Order;
import com.sybanh.demo_website_tramhuong.entity.OrderDetail;
import com.sybanh.demo_website_tramhuong.entity.OrderStatus;
import com.sybanh.demo_website_tramhuong.entity.Product;
import com.sybanh.demo_website_tramhuong.entity.Role;
import com.sybanh.demo_website_tramhuong.entity.User;
import com.sybanh.demo_website_tramhuong.exception.EmptyCartException;
import com.sybanh.demo_website_tramhuong.exception.InsufficientStockException;
import com.sybanh.demo_website_tramhuong.exception.ResourceNotFoundException;
import com.sybanh.demo_website_tramhuong.repository.CartItemRepository;
import com.sybanh.demo_website_tramhuong.repository.CartRepository;
import com.sybanh.demo_website_tramhuong.repository.OrderDetailRepository;
import com.sybanh.demo_website_tramhuong.repository.OrderRepository;
import com.sybanh.demo_website_tramhuong.repository.ProductRepository;
import com.sybanh.demo_website_tramhuong.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private User adminUser;
    private Product product;
    private Cart cart;
    private Order order;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder().roleId(1L).roleName("USER").build();
        Role adminRole = Role.builder().roleId(2L).roleName("ADMIN").build();

        user = User.builder().userId(1L).email("user@example.com").address("1 User St").roles(userRole).build();
        adminUser = User.builder().userId(2L).email("admin@example.com").roles(adminRole).build();

        product = Product.builder()
                .productId(10L)
                .productName("Tram Huong A")
                .productPrice(BigDecimal.valueOf(50000))
                .productQuantity(5)
                .active(true)
                .build();

        cart = Cart.builder().cartId(100L).user(user).build();

        order = Order.builder()
                .orderId(1L)
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100000))
                .shippingAddress("1 User St")
                .build();
    }

    // ---------- checkout ----------

    @Test
    void checkout_nemEmptyCartException_khiChuaCoCart() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.checkout("user@example.com", null))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void checkout_nemEmptyCartException_khiCartRong() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(List.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.checkout("user@example.com", null))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void checkout_nemInsufficientStockException_khiVuotTonKho() {
        // tồn kho chỉ có 5, giỏ hàng đang có 10
        CartItem cartItem = CartItem.builder().cart(cart).product(product).quantity(10).build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(List.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.checkout("user@example.com", null))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void checkout_thanhCong_truTonKhoVaXoaGioHang() {
        CartItem cartItem = CartItem.builder().cart(cart).product(product).quantity(2).build();
        CheckoutRequest request = CheckoutRequest.builder().shippingAddress("Da Nang").build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(List.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.save(any(OrderDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.findByOrder(any(Order.class))).thenReturn(List.of(
                OrderDetail.builder().order(order).product(product).price(product.getProductPrice()).quantity(2)
                        .build()));

        OrderResponse response = orderService.checkout("user@example.com", request);

        assertThat(response.getShippingAddress()).isEqualTo("Da Nang");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        // tồn kho phải giảm đúng 2 (5 - 2 = 3)
        assertThat(product.getProductQuantity()).isEqualTo(3);
        // giỏ hàng phải bị dọn sạch sau khi checkout
        verify(cartItemRepository).deleteAll(List.of(cartItem));
    }

    // ---------- getOrders ----------

    @Test
    void getOrders_traVeDanhSachDonHang() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByUser(user)).thenReturn(List.of(order));
        when(orderDetailRepository.findByOrder(order)).thenReturn(List.of());

        List<OrderResponse> responses = orderService.getOrders("user@example.com");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getOrderId()).isEqualTo(1L);
    }

    // ---------- getOrderById ----------

    @Test
    void getOrderById_chuDonXemDuoc() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrder(order)).thenReturn(List.of());

        OrderResponse response = orderService.getOrderById("user@example.com", 1L);

        assertThat(response.getOrderId()).isEqualTo(1L);
    }

    @Test
    void getOrderById_nemResourceNotFoundException_khiKhongPhaiChuVaKhongPhaiAdmin() {
        User nguoiKhac = User.builder().userId(99L).email("nguoikhac@example.com")
                .roles(Role.builder().roleId(1L).roleName("USER").build()).build();

        when(userRepository.findByEmail("nguoikhac@example.com")).thenReturn(Optional.of(nguoiKhac));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById("nguoikhac@example.com", 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOrderById_adminXemDuocDonNguoiKhac() {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrder(order)).thenReturn(List.of());

        OrderResponse response = orderService.getOrderById("admin@example.com", 1L);

        assertThat(response.getOrderId()).isEqualTo(1L);
    }

    // ---------- updateStatus ----------

    @Test
    void updateStatus_thanhCong() {
        OrderStatusUpdateRequest request = OrderStatusUpdateRequest.builder().status(OrderStatus.CONFIRMED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrder(order)).thenReturn(List.of());

        OrderResponse response = orderService.updateStatus(1L, request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateStatus_nemResourceNotFoundException_khiKhongTonTai() {
        OrderStatusUpdateRequest request = OrderStatusUpdateRequest.builder().status(OrderStatus.CONFIRMED).build();
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        // không tìm thấy đơn thì tuyệt đối không được lưu gì cả
        verify(orderRepository, never()).save(any(Order.class));
    }
}
