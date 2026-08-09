package com.sybanh.demo_website_tramhuong.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sybanh.demo_website_tramhuong.dto.request.CartItemRequest;
import com.sybanh.demo_website_tramhuong.dto.response.CartResponse;
import com.sybanh.demo_website_tramhuong.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/carts")
public class CartController {

    private final CartService cartService;

    @GetMapping("")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return new ResponseEntity<>(cartService.getCart(userDetails.getUsername()), HttpStatus.OK);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest cartItemRequest) {

        String email = userDetails.getUsername();
        return new ResponseEntity<>(cartService.addItem(email, cartItemRequest), HttpStatus.CREATED);
    }

    @PutMapping("items/{cartItemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemRequest cartItemRequest) {

        return new ResponseEntity<>(
                cartService.updateItemQuantity(userDetails.getUsername(), cartItemId, cartItemRequest), HttpStatus.OK);
    }

    @DeleteMapping("items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {

        return new ResponseEntity<>(cartService.removeItem(userDetails.getUsername(), cartItemId), HttpStatus.OK);
    }

}
