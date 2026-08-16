package com.sybanh.demo_website_tramhuong.service;

import java.util.List;

import com.sybanh.demo_website_tramhuong.dto.request.UpdateUserRoleRequest;
import com.sybanh.demo_website_tramhuong.dto.response.UserResponse;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long userId);

    UserResponse updateUserRole(Long userId, UpdateUserRoleRequest request);

}
