package com.sybanh.demo_website_tramhuong.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sybanh.demo_website_tramhuong.dto.request.UpdateUserRoleRequest;
import com.sybanh.demo_website_tramhuong.dto.response.UserResponse;
import com.sybanh.demo_website_tramhuong.entity.Role;
import com.sybanh.demo_website_tramhuong.entity.User;
import com.sybanh.demo_website_tramhuong.exception.ResourceNotFoundException;
import com.sybanh.demo_website_tramhuong.repository.RoleRepository;
import com.sybanh.demo_website_tramhuong.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder().roleId(1L).roleName("USER").build();
        user = User.builder()
                .userId(1L)
                .email("sybanh@gmail.com")
                .fullName("nguyenngocsy")
                .address("da nang")
                .phoneNumber("0354806808")
                .roles(role)
                .build();
    }

    @Test
    void getUserById_traVeDung_khiTonTai() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserResponse response = userService.getUserById(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("sybanh@gmail.com");
    }

    @Test
    void getUserById_nemResourceNotFoundException_khiKhongTonTai() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllUser_traVeDanhSach() {
        User user2 = User.builder()
                .userId(2L)
                .email("synguyen@gmail.com")
                .fullName("nguyenngocsy")
                .address("da nang")
                .phoneNumber("0354806808")
                .roles(role)
                .build();
        when(userRepository.findAll()).thenReturn(List.of(user, user2));
        List<UserResponse> responses = userService.getAllUsers();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getEmail()).isEqualTo("sybanh@gmail.com");
        assertThat(responses.get(1).getEmail()).isEqualTo("synguyen@gmail.com");
    }

    @Test
    void updateUserRole_success() {
        UpdateUserRoleRequest request = UpdateUserRoleRequest.builder()
                .roleName("USER")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUserRole(1L, request);
        assertThat(response.getRoleName()).isEqualTo("USER");
    }

    @Test
    void updateUserRole_nemResourceNotFoundException_khiKhongTonTai() {
        UpdateUserRoleRequest request = UpdateUserRoleRequest.builder()
                .roleName("ADMIN")
                .build();
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(9L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
