package com.sybanh.demo_website_tramhuong.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sybanh.demo_website_tramhuong.dto.request.PaymentRequest;
import com.sybanh.demo_website_tramhuong.dto.response.PaymentResponse;
import com.sybanh.demo_website_tramhuong.entity.Order;
import com.sybanh.demo_website_tramhuong.entity.OrderStatus;
import com.sybanh.demo_website_tramhuong.entity.Payment;
import com.sybanh.demo_website_tramhuong.entity.PaymentStatus;
import com.sybanh.demo_website_tramhuong.entity.User;
import com.sybanh.demo_website_tramhuong.exception.OrderAlreadyPaidException;
import com.sybanh.demo_website_tramhuong.exception.ResourceNotFoundException;
import com.sybanh.demo_website_tramhuong.repository.OrderRepository;
import com.sybanh.demo_website_tramhuong.repository.PaymentRepository;
import com.sybanh.demo_website_tramhuong.repository.UserRepository;
import com.sybanh.demo_website_tramhuong.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public PaymentResponse pay(String email, PaymentRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found: " + email));
        Order order = orderRepository.findById(request.getOrderId()).orElseThrow(
                () -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        boolean isOwner = order.getUser().getUserId().equals(user.getUserId());
        boolean isAdmin = user.getRoles().getRoleName().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new ResourceNotFoundException("Order not found: " + request.getOrderId());
        }

        boolean alreadyPaid = paymentRepository.findByOrder(order).stream()
                .anyMatch(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS);
        if (alreadyPaid) {
            throw new OrderAlreadyPaidException("Order already paid: " + order.getOrderId());
        }

        Payment payment = paymentRepository.save(Payment.builder()
                .user(user)
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .amount(order.getTotalAmount())
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionCode(UUID.randomUUID().toString())
                .paidAt(LocalDateTime.now())
                .build());

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        return toPaymentResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPayments(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found: " + email));

        return paymentRepository.findByUser(user).stream()
                .map(this::toPaymentResponse).toList();
    }

    @Override
    public PaymentResponse getPaymentById(String email, Long paymentId) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found: " + email));
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new ResourceNotFoundException("Payment not found: " + paymentId));
        boolean isOwner = payment.getUser().getUserId().equals(user.getUserId());
        boolean isAdmin = user.getRoles().getRoleName().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new ResourceNotFoundException("Payment not found: " + paymentId);
        }
        return toPaymentResponse(payment);
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder().getOrderId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .transactionCode(payment.getTransactionCode())
                .paidAt(payment.getPaidAt())
                .build();
    }

}
