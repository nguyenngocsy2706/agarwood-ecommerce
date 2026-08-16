package com.sybanh.demo_website_tramhuong.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

import com.sybanh.demo_website_tramhuong.dto.request.PaymentRequest;
import com.sybanh.demo_website_tramhuong.dto.response.PaymentResponse;
import com.sybanh.demo_website_tramhuong.entity.Order;
import com.sybanh.demo_website_tramhuong.entity.OrderStatus;
import com.sybanh.demo_website_tramhuong.entity.Payment;
import com.sybanh.demo_website_tramhuong.entity.PaymentStatus;
import com.sybanh.demo_website_tramhuong.entity.Role;
import com.sybanh.demo_website_tramhuong.entity.User;
import com.sybanh.demo_website_tramhuong.exception.OrderAlreadyPaidException;
import com.sybanh.demo_website_tramhuong.exception.ResourceNotFoundException;
import com.sybanh.demo_website_tramhuong.repository.OrderRepository;
import com.sybanh.demo_website_tramhuong.repository.PaymentRepository;
import com.sybanh.demo_website_tramhuong.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User user;
    private User adminUser;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder().roleId(1L).roleName("USER").build();
        Role adminRole = Role.builder().roleId(2L).roleName("ADMIN").build();

        user = User.builder().userId(1L).email("user@example.com").roles(userRole).build();
        adminUser = User.builder().userId(2L).email("admin@example.com").roles(adminRole).build();

        order = Order.builder()
                .orderId(1L)
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        payment = Payment.builder()
                .paymentId(5L)
                .user(user)
                .order(order)
                .paymentMethod("COD")
                .amount(BigDecimal.valueOf(100000))
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
    }

    // ---------- pay ----------

    @Test
    void pay_nemResourceNotFoundException_khiOrderKhongTonTai() {
        PaymentRequest request = PaymentRequest.builder().orderId(99L).paymentMethod("COD").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.pay("user@example.com", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void pay_nemResourceNotFoundException_khiKhongPhaiChuDon() {
        User nguoiKhac = User.builder().userId(99L).email("nguoikhac@example.com")
                .roles(Role.builder().roleId(1L).roleName("USER").build()).build();
        PaymentRequest request = PaymentRequest.builder().orderId(1L).paymentMethod("COD").build();

        when(userRepository.findByEmail("nguoikhac@example.com")).thenReturn(Optional.of(nguoiKhac));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.pay("nguoikhac@example.com", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void pay_nemOrderAlreadyPaidException_khiDaThanhToanRoi() {
        PaymentRequest request = PaymentRequest.builder().orderId(1L).paymentMethod("COD").build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(List.of(payment)); // đã có SUCCESS từ trước

        assertThatThrownBy(() -> paymentService.pay("user@example.com", request))
                .isInstanceOf(OrderAlreadyPaidException.class);

        // đã thanh toán rồi thì không được tạo thêm payment mới
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void pay_thanhCong_donTuChuyenConfirmed() {
        PaymentRequest request = PaymentRequest.builder().orderId(1L).paymentMethod("COD").build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(List.of()); // chưa thanh toán lần nào
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.pay("user@example.com", request);

        assertThat(response.getAmount()).isEqualByComparingTo("100000");
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        // order đang PENDING lúc thanh toán -> phải tự chuyển CONFIRMED
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CONFIRMED));
    }

    // ---------- getPayments ----------

    @Test
    void getPayments_traVeDanhSach() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(paymentRepository.findByUser(user)).thenReturn(List.of(payment));

        List<PaymentResponse> responses = paymentService.getPayments("user@example.com");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getPaymentId()).isEqualTo(5L);
    }

    // ---------- getPaymentById ----------

    @Test
    void getPaymentById_chuThanhToanXemDuoc() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById("user@example.com", 5L);

        assertThat(response.getPaymentId()).isEqualTo(5L);
    }

    @Test
    void getPaymentById_nemResourceNotFoundException_khiKhongPhaiChuVaKhongPhaiAdmin() {
        User nguoiKhac = User.builder().userId(99L).email("nguoikhac@example.com")
                .roles(Role.builder().roleId(1L).roleName("USER").build()).build();

        when(userRepository.findByEmail("nguoikhac@example.com")).thenReturn(Optional.of(nguoiKhac));
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.getPaymentById("nguoikhac@example.com", 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPaymentById_adminXemDuocPaymentNguoiKhac() {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById("admin@example.com", 5L);

        assertThat(response.getPaymentId()).isEqualTo(5L);
    }
}
