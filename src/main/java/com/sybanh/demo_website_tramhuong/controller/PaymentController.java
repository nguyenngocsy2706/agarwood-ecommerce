package com.sybanh.demo_website_tramhuong.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sybanh.demo_website_tramhuong.dto.request.PaymentRequest;
import com.sybanh.demo_website_tramhuong.dto.response.PaymentResponse;
import com.sybanh.demo_website_tramhuong.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("")
    public ResponseEntity<PaymentResponse> pay(@AuthenticationPrincipal User user,
            @Valid @RequestBody PaymentRequest request) {

        return new ResponseEntity<>(paymentService.pay(user.getUsername(), request), HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<PaymentResponse>> getPayments(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(paymentService.getPayments(user.getUsername()), HttpStatus.OK);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@AuthenticationPrincipal User user,
            @PathVariable Long paymentId) {
        return new ResponseEntity<>(paymentService.getPaymentById(user.getUsername(), paymentId), HttpStatus.OK);
    }

}
