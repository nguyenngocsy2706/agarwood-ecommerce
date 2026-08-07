package com.sybanh.demo_website_tramhuong.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sybanh.demo_website_tramhuong.dto.auth.LoginRequest;
import com.sybanh.demo_website_tramhuong.dto.auth.RegisterRequest;
import com.sybanh.demo_website_tramhuong.entity.Role;
import com.sybanh.demo_website_tramhuong.entity.User;
import com.sybanh.demo_website_tramhuong.repository.RoleRepository;
import com.sybanh.demo_website_tramhuong.repository.UserRepository;
import com.sybanh.demo_website_tramhuong.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .fullName(request.getFullName())
                .build();

        Role role = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(role);

        return userRepository.save(user);
    }

    @Override
    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return user;

    }

}
