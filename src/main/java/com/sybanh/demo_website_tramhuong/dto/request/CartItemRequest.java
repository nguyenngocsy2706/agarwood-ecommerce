package com.sybanh.demo_website_tramhuong.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
