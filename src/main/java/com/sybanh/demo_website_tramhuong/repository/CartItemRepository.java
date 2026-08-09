package com.sybanh.demo_website_tramhuong.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sybanh.demo_website_tramhuong.entity.Cart;
import com.sybanh.demo_website_tramhuong.entity.CartItem;
import com.sybanh.demo_website_tramhuong.entity.Product;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

}
