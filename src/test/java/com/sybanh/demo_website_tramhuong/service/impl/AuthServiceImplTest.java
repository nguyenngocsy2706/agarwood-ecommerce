package com.sybanh.demo_website_tramhuong.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role userRole;
    private User user;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().roleId(1L).roleName("USER").build();
        user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .password("da-ma-hoa")
                .roles(userRole)
                .build();
    }

    // ---------- register ----------

    @Test
    void register_nemEmailAlreadyExistsException_khiTrungEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .phoneNumber("0900000000")
                .address("1 Test St")
                .build();
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        // email đã tồn tại thì tuyệt đối không được tạo user mới
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_thanhCong_traVeAccessToken() {
        RegisterRequest request = RegisterRequest.builder()
                .email("moi@example.com")
                .password("password123")
                .fullName("Nguoi Moi")
                .phoneNumber("0911111111")
                .address("2 Moi St")
                .build();

        when(userRepository.existsByEmail("moi@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash-cua-password123");
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken("moi@example.com")).thenReturn("token-gia-lap");

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("token-gia-lap");
        // password lưu vào DB phải là bản đã hash, không phải password gốc
        verify(userRepository).save(argThat(u -> u.getPassword().equals("hash-cua-password123")));
    }

    // ---------- login ----------

    @Test
    void login_nemInvalidCredentialsException_khiKhongTimThayUser() {
        LoginRequest request = LoginRequest.builder().email("khongton@example.com").password("password123").build();
        when(userRepository.findByEmail("khongton@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_nemInvalidCredentialsException_khiSaiPassword() {
        LoginRequest request = LoginRequest.builder().email("test@example.com").password("sai-password").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("sai-password", "da-ma-hoa")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_thanhCong_traVeAccessToken() {
        LoginRequest request = LoginRequest.builder().email("test@example.com").password("dung-password").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("dung-password", "da-ma-hoa")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("token-gia-lap");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("token-gia-lap");
    }
}
