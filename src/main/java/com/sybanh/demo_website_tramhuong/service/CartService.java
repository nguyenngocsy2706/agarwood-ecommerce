package com.sybanh.demo_website_tramhuong.service;

import com.sybanh.demo_website_tramhuong.dto.request.CartItemRequest;
import com.sybanh.demo_website_tramhuong.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(String email);

    CartResponse addItem(String email, CartItemRequest cartItemRequest);

    CartResponse updateItemQuantity(String email, Long cartItemId, CartItemRequest cartItemRequest);

    CartResponse removeItem(String email, Long cartItemId);

}
