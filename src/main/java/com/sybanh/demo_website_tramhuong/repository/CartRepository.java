package com.sybanh.demo_website_tramhuong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sybanh.demo_website_tramhuong.entity.Cart;
import com.sybanh.demo_website_tramhuong.entity.User;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUser(User user);
}
