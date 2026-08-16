package com.sybanh.demo_website_tramhuong.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sybanh.demo_website_tramhuong.dto.auth.AuthResponse;
import com.sybanh.demo_website_tramhuong.dto.auth.LoginRequest;
import com.sybanh.demo_website_tramhuong.dto.auth.RegisterRequest;
import com.sybanh.demo_website_tramhuong.entity.Role;
import com.sybanh.demo_website_tramhuong.entity.User;
import com.sybanh.demo_website_tramhuong.exception.EmailAlreadyExistsException;
import com.sybanh.demo_website_tramhuong.exception.InvalidCredentialsException;
import com.sybanh.demo_website_tramhuong.repository.RoleRepository;
import com.sybanh.demo_website_tramhuong.repository.UserRepository;
import com.sybanh.demo_website_tramhuong.security.JwtUtil;
import com.sybanh.demo_website_tramhuong.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
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

        User userSave = userRepository.save(user);
        String token = jwtUtil.generateToken(userSave.getEmail());
        return AuthResponse.builder()
                .accessToken(token)
                .build();

    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .accessToken(token)
                .build();

    }

}
