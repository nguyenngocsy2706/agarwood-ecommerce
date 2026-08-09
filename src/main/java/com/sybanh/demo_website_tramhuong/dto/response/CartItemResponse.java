package com.sybanh.demo_website_tramhuong.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
