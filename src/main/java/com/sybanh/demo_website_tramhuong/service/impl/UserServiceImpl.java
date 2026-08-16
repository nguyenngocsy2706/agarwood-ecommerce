package com.sybanh.demo_website_tramhuong.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sybanh.demo_website_tramhuong.dto.request.UpdateUserRoleRequest;
import com.sybanh.demo_website_tramhuong.dto.response.UserResponse;
import com.sybanh.demo_website_tramhuong.entity.Role;
import com.sybanh.demo_website_tramhuong.entity.User;
import com.sybanh.demo_website_tramhuong.exception.ResourceNotFoundException;
import com.sybanh.demo_website_tramhuong.repository.RoleRepository;
import com.sybanh.demo_website_tramhuong.repository.UserRepository;
import com.sybanh.demo_website_tramhuong.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Not found user: " + userId));
        return toUserResponse(user);
    }

    @Override
    public UserResponse updateUserRole(Long userId, UpdateUserRoleRequest request) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Not found user: " + userId));
        Role role = roleRepository.findByRoleName(request.getRoleName()).orElseThrow(
                () -> new ResourceNotFoundException("Not found role: " + request.getRoleName()));
        user.setRoles(role);
        userRepository.save(user);
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .roleName(user.getRoles().getRoleName())
                .build();
    }

}
