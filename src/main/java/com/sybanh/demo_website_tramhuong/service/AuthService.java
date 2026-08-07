package com.sybanh.demo_website_tramhuong.service;

import com.sybanh.demo_website_tramhuong.dto.auth.LoginRequest;
import com.sybanh.demo_website_tramhuong.dto.auth.RegisterRequest;
import com.sybanh.demo_website_tramhuong.entity.User;

public interface AuthService {
    public User register(RegisterRequest request);

    public User login(LoginRequest request);
}
