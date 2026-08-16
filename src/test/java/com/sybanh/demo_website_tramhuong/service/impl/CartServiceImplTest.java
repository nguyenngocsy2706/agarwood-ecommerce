package com.sybanh.demo_website_tramhuong.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import com.sybanh.demo_website_tramhuong.dto.request.CartItemRequest;
import com.sybanh.demo_website_tramhuong.dto.response.CartResponse;
import com.sybanh.demo_website_tramhuong.entity.Cart;
import com.sybanh.demo_website_tramhuong.entity.CartItem;
import com.sybanh.demo_website_tramhuong.entity.Product;
import com.sybanh.demo_website_tramhuong.entity.User;
import com.sybanh.demo_website_tramhuong.exception.InsufficientStockException;
import com.sybanh.demo_website_tramhuong.exception.ResourceNotFoundException;
import com.sybanh.demo_website_tramhuong.repository.CartItemRepository;
import com.sybanh.demo_website_tramhuong.repository.CartRepository;
import com.sybanh.demo_website_tramhuong.repository.ProductRepository;
import com.sybanh.demo_website_tramhuong.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder().userId(1L).email("test@example.com").build();
        product = Product.builder()
                .productId(10L)
                .productName("Tram Huong A")
                .productPrice(BigDecimal.valueOf(50000))
                .productQuantity(5)
                .active(true)
                .build();
        cart = Cart.builder().cartId(100L).user(user).build();
    }

    @Test
    void addItem_taoMoi_khiSanPhamChuaCoTrongGio() {
        // Arrange: chuẩn bị dữ liệu + dặn các mock trả lời gì khi được gọi
        CartItemRequest request = new CartItemRequest(10L, 2);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(user)).thenReturn(List.of(cart));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of(
                CartItem.builder().cart(cart).product(product).quantity(2).build()));

        // Act: gọi hàm thật sự cần test
        CartResponse response = cartService.addItem("test@example.com", request);

        // Assert: kiểm tra kết quả
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("100000");
    }

    @Test
    void addItem_nemInsufficientStockException_khiVuotTonKho() {
        // product tồn kho chỉ có 5 (setUp), yêu cầu mua 10 -> phải bị chặn
        CartItemRequest request = new CartItemRequest(10L, 10);
        CartItem existingItem = CartItem.builder().cart(cart).product(product).quantity(0).build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(user)).thenReturn(List.of(cart));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(existingItem));

        assertThatThrownBy(() -> cartService.addItem("test@example.com", request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Not enough stock");
    }

    @Test
    void getCart_traVeCartCoSan_khiUserDaCoCart() {
        // User đã có cart từ trước -> getCart KHÔNG được tạo cart mới, chỉ lấy cart cũ ra dùng
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(List.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of());

        CartResponse response = cartService.getCart("test@example.com");

        assertThat(response.getCartId()).isEqualTo(100L);
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void getCart_taoCartMoi_khiUserChuaCoCart() {
        // User chưa có cart nào -> getCart phải tự tạo mới rồi lưu lại
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(List.of());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of());

        CartResponse response = cartService.getCart("test@example.com");

        assertThat(response.getCartId()).isEqualTo(100L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void removeItem_xoaThanhCong_khiDungChuSoHuu() {
        CartItem cartItem = CartItem.builder().cartItemId(5L).cart(cart).product(product).quantity(2).build();

        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findByCart(cart)).thenReturn(List.of()); // sau khi xoá, giỏ hàng rỗng

        CartResponse response = cartService.removeItem("test@example.com", 5L);

        verify(cartItemRepository).delete(cartItem);
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void removeItem_nemResourceNotFoundException_khiKhongPhaiChuSoHuu() {
        CartItem cartItem = CartItem.builder().cartItemId(5L).cart(cart).product(product).quantity(2).build();

        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(cartItem));

        // cart này là của "test@example.com", nhưng gọi bằng email khác
        assertThatThrownBy(() -> cartService.removeItem("nguoi-khac@example.com", 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateItemQuantity_nemInsufficientStockException_khiVuotTonKho() {
        // product tồn kho chỉ có 5 (setUp)
        CartItem cartItem = CartItem.builder().cartItemId(5L).cart(cart).product(product).quantity(1).build();
        CartItemRequest request = new CartItemRequest(10L, 10);

        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(cartItem));

        assertThatThrownBy(() -> cartService.updateItemQuantity("test@example.com", 5L, request))
                .isInstanceOf(InsufficientStockException.class);
    }
}
