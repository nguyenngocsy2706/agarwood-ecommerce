package com.sybanh.demo_website_tramhuong.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sybanh.demo_website_tramhuong.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private String transactionCode;
    private LocalDateTime paidAt;
}
