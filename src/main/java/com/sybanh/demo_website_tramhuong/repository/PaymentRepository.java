package com.sybanh.demo_website_tramhuong.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sybanh.demo_website_tramhuong.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
