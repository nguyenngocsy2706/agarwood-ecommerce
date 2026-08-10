package com.sybanh.demo_website_tramhuong.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sybanh.demo_website_tramhuong.dto.request.CheckoutRequest;
import com.sybanh.demo_website_tramhuong.dto.request.OrderStatusUpdateRequest;
import com.sybanh.demo_website_tramhuong.dto.response.OrderResponse;
import com.sybanh.demo_website_tramhuong.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("")
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal User user,
            @Valid @RequestBody(required = false) CheckoutRequest request) {

        return new ResponseEntity<>(orderService.checkout(user.getUsername(), request), HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<OrderResponse>> getOrders(@AuthenticationPrincipal User user) {

        return new ResponseEntity<>(orderService.getOrders(user.getUsername()), HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrders(@PathVariable Long orderId, @AuthenticationPrincipal User user) {

        return new ResponseEntity<>(orderService.getOrderById(user.getUsername(), orderId), HttpStatus.OK);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {

        return new ResponseEntity<>(orderService.updateStatus(orderId, request), HttpStatus.OK);
    }

}
