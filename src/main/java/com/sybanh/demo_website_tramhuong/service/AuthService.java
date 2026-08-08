package com.sybanh.demo_website_tramhuong.service;

import com.sybanh.demo_website_tramhuong.dto.auth.AuthResponse;
import com.sybanh.demo_website_tramhuong.dto.auth.LoginRequest;
import com.sybanh.demo_website_tramhuong.dto.auth.RegisterRequest;

public interface AuthService {
    public AuthResponse register(RegisterRequest request);

    public AuthResponse login(LoginRequest request);
}
