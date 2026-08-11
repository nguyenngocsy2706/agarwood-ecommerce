package com.sybanh.demo_website_tramhuong.service;

import java.util.List;

import com.sybanh.demo_website_tramhuong.dto.request.PaymentRequest;
import com.sybanh.demo_website_tramhuong.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse pay(String email, PaymentRequest request);

    List<PaymentResponse> getPayments(String email);

    PaymentResponse getPaymentById(String email, Long paymentId);
}
