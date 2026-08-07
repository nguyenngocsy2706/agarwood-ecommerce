package com.sybanh.demo_website_tramhuong.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sybanh.demo_website_tramhuong.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}
