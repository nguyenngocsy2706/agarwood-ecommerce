package com.sybanh.demo_website_tramhuong.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sybanh.demo_website_tramhuong.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

}
