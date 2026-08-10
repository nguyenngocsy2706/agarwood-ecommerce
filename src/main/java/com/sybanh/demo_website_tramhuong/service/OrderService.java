package com.sybanh.demo_website_tramhuong.service;

import java.util.List;

import com.sybanh.demo_website_tramhuong.dto.request.CheckoutRequest;
import com.sybanh.demo_website_tramhuong.dto.request.OrderStatusUpdateRequest;
import com.sybanh.demo_website_tramhuong.dto.response.OrderResponse;

public interface OrderService {
    OrderResponse checkout(String email, CheckoutRequest request);

    List<OrderResponse> getOrders(String email);

    OrderResponse getOrderById(String email, Long orderId);

    OrderResponse updateStatus(Long orderId, OrderStatusUpdateRequest request);
}
