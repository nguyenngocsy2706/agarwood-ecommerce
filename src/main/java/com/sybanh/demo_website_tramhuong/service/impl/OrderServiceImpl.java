package com.sybanh.demo_website_tramhuong.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sybanh.demo_website_tramhuong.dto.request.CheckoutRequest;
import com.sybanh.demo_website_tramhuong.dto.request.OrderStatusUpdateRequest;
import com.sybanh.demo_website_tramhuong.dto.response.OrderDetailResponse;
import com.sybanh.demo_website_tramhuong.dto.response.OrderResponse;
import com.sybanh.demo_website_tramhuong.entity.Cart;
import com.sybanh.demo_website_tramhuong.entity.CartItem;
import com.sybanh.demo_website_tramhuong.entity.Order;
import com.sybanh.demo_website_tramhuong.entity.OrderDetail;
import com.sybanh.demo_website_tramhuong.entity.OrderStatus;
import com.sybanh.demo_website_tramhuong.entity.Product;
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
import com.sybanh.demo_website_tramhuong.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse checkout(String email, CheckoutRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + email));

        Cart cart = cartRepository.findByUser(user).stream().findFirst()
                .orElseThrow(() -> new EmptyCartException("Cart is empty"));
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Cart is empty");
        }

        for (CartItem cartItem : cartItems) {
            if (cartItem.getQuantity() > cartItem.getProduct().getProductQuantity()) {
                throw new InsufficientStockException(
                        "Not enough stock for product: " + cartItem.getProduct().getProductName());
            }
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String shippingAddress = request != null && request.getShippingAddress() != null
                && !request.getShippingAddress().isBlank()
                        ? request.getShippingAddress()
                        : user.getAddress();

        Order order = orderRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingAddress(shippingAddress)
                .build());

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            orderDetailRepository.save(OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .price(product.getProductPrice())
                    .quantity(cartItem.getQuantity())
                    .build());

            product.setProductQuantity(product.getProductQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        cartItemRepository.deleteAll(cartItems);

        return toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + email));
        return orderRepository.findByUser(user).stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderById(String email, Long orderId) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + email));
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ResourceNotFoundException("Order not found: " + orderId));

        boolean isOwner = order.getUser().getUserId().equals(user.getUserId());
        boolean isAdmin = user.getRoles().getRoleName().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }
        return toOrderResponse(order);
    }

    @Override
    public OrderResponse updateStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setStatus(request.getStatus());
        orderRepository.save(order);
        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderDetailResponse> items = orderDetailRepository.findByOrder(order).stream()
                .map(this::toOrderDetailResponse)
                .toList();
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail) {
        return OrderDetailResponse.builder()
                .orderDetailId(orderDetail.getOrderDetailId())
                .productId(orderDetail.getProduct().getProductId())
                .productName(orderDetail.getProduct().getProductName())
                .productPrice(orderDetail.getPrice())
                .quantity(orderDetail.getQuantity())
                .subtotal(
                        (orderDetail.getPrice()).multiply(BigDecimal.valueOf(orderDetail.getQuantity())))
                .build();
    }
}
