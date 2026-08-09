package com.sybanh.demo_website_tramhuong.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sybanh.demo_website_tramhuong.dto.request.CartItemRequest;
import com.sybanh.demo_website_tramhuong.dto.response.CartItemResponse;
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
import com.sybanh.demo_website_tramhuong.service.CartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;

        @Override
        public CartResponse getCart(String email) {
                User user = userRepository.findByEmail(email).orElseThrow(
                                () -> new ResourceNotFoundException("User not found with email: " + email));
                Cart cart = cartRepository.findByUser(user).stream()
                                .findFirst()
                                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
                return toCartResponse(cart);
        }

        @Override
        public CartResponse addItem(String email, CartItemRequest cartItemRequest) {

                User user = userRepository.findByEmail(email).orElseThrow(
                                () -> new ResourceNotFoundException("User not found with email: " + email));
                Product product = productRepository.findById(cartItemRequest.getProductId())
                                .filter(Product::isActive)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "Not found Product with productId: "
                                                                                + cartItemRequest.getProductId()));

                Cart cart = cartRepository.findByUser(user).stream()
                                .findFirst()
                                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

                CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                                .orElseGet(() -> CartItem.builder()
                                                .cart(cart)
                                                .product(product)
                                                .quantity(0)
                                                .build());

                int newQuantity = cartItem.getQuantity() + cartItemRequest.getQuantity();
                if (newQuantity > product.getProductQuantity()) {
                        throw new InsufficientStockException(
                                        "Not enough stock for product: " + product.getProductName());
                }
                cartItem.setQuantity(newQuantity);

                cartItemRepository.save(cartItem);
                return toCartResponse(cart);
        }

        @Override
        public CartResponse updateItemQuantity(String email, Long cartItemId, CartItemRequest cartItemRequest) {
                CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
                                () -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

                if (!cartItem.getCart().getUser().getEmail().equals(email)) {
                        throw new ResourceNotFoundException("Cart item not found: " + cartItemId);
                }

                if (cartItemRequest.getQuantity() > cartItem.getProduct().getProductQuantity()) {
                        throw new InsufficientStockException(
                                        "Not enough stock for product: " + cartItem.getProduct().getProductName());
                }
                cartItem.setQuantity(cartItemRequest.getQuantity());

                cartItemRepository.save(cartItem);

                return toCartResponse(cartItem.getCart());

        }

        @Override
        public CartResponse removeItem(String email, Long cartItemId) {
                CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
                                () -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

                if (!cartItem.getCart().getUser().getEmail().equals(email)) {
                        throw new ResourceNotFoundException("Cart item not found: " + cartItemId);
                }

                Cart cart = cartItem.getCart();
                cartItemRepository.delete(cartItem);
                return toCartResponse(cart);
        }

        private CartResponse toCartResponse(Cart cart) {
                List<CartItemResponse> items = cartItemRepository.findByCart(cart).stream()
                                .map(this::toCartItemResponse)
                                .toList();
                BigDecimal totalAmount = items.stream()
                                .map(CartItemResponse::getSubtotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                return CartResponse.builder()
                                .items(items)
                                .cartId(cart.getCartId())
                                .totalAmount(totalAmount)
                                .build();
        }

        private CartItemResponse toCartItemResponse(CartItem cartItem) {
                return CartItemResponse.builder()
                                .cartItemId(cartItem.getCartItemId())
                                .productId(cartItem.getProduct().getProductId())
                                .productName(cartItem.getProduct().getProductName())
                                .productPrice(cartItem.getProduct().getProductPrice())
                                .quantity(cartItem.getQuantity())
                                .subtotal(
                                                (cartItem.getProduct().getProductPrice())
                                                                .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                                .build();
        }
}
