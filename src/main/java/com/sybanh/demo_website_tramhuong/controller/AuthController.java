package com.sybanh.demo_website_tramhuong.controller;

import org.springframework.web.bind.annotation.RestController;

import com.sybanh.demo_website_tramhuong.dto.auth.LoginRequest;
import com.sybanh.demo_website_tramhuong.dto.auth.RegisterRequest;
import com.sybanh.demo_website_tramhuong.entity.User;
import com.sybanh.demo_website_tramhuong.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        // TODO: process POST request
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("login")
    public ResponseEntity<User> login(@Valid @RequestBody LoginRequest request) {
        // TODO: process POST request
        return ResponseEntity.ok(authService.login(request));
    }

}
