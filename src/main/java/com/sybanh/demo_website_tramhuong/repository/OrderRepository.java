package com.sybanh.demo_website_tramhuong.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sybanh.demo_website_tramhuong.entity.Order;
import com.sybanh.demo_website_tramhuong.entity.User;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
