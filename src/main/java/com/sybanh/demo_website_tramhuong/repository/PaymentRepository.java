package com.sybanh.demo_website_tramhuong.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sybanh.demo_website_tramhuong.entity.Order;
import com.sybanh.demo_website_tramhuong.entity.Payment;
import com.sybanh.demo_website_tramhuong.entity.User;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrder(Order order);

    List<Payment> findByUser(User user);
}
