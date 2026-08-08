package com.sybanh.demo_website_tramhuong.service;

import java.util.List;

import com.sybanh.demo_website_tramhuong.dto.request.ProductRequest;
import com.sybanh.demo_website_tramhuong.dto.response.ProductResponse;

public interface ProductService {
    List<ProductResponse> getAll(Long categoryId);

    ProductResponse getById(Long productId);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long productId, ProductRequest productRequest);

    void delete(Long productId);
}
