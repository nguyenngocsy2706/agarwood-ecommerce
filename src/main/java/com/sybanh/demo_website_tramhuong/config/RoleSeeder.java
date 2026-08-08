package com.sybanh.demo_website_tramhuong.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sybanh.demo_website_tramhuong.entity.Role;
import com.sybanh.demo_website_tramhuong.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRoleIfMissing("USER");
        createRoleIfMissing("ADMIN");
    }

    private void createRoleIfMissing(String roleName) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            roleRepository.save(Role.builder().roleName(roleName).build());
        }
    }
}
