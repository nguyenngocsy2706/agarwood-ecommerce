package com.sybanh.demo_website_tramhuong.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sybanh.demo_website_tramhuong.entity.Order;
import com.sybanh.demo_website_tramhuong.entity.OrderDetail;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrder(Order order);
}
