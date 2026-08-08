package com.sybanh.demo_website_tramhuong.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank
    private String productName;

    @NotBlank
    private String productDescription;

    @NotNull
    @Positive
    private BigDecimal productPrice;

    @NotNull
    @Min(0)
    private Integer productQuantity;

    @NotNull
    private Long categoryId;
}
