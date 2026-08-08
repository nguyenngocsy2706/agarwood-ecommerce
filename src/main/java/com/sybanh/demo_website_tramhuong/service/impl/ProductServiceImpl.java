package com.sybanh.demo_website_tramhuong.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sybanh.demo_website_tramhuong.dto.request.ProductRequest;
import com.sybanh.demo_website_tramhuong.dto.response.ProductResponse;
import com.sybanh.demo_website_tramhuong.entity.Category;
import com.sybanh.demo_website_tramhuong.entity.Product;
import com.sybanh.demo_website_tramhuong.exception.ResourceNotFoundException;
import com.sybanh.demo_website_tramhuong.repository.CategoryRepository;
import com.sybanh.demo_website_tramhuong.repository.ProductRepository;
import com.sybanh.demo_website_tramhuong.service.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<ProductResponse> getAll(Long categoryId) {
        List<Product> products;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
            products = productRepository.findByCategoryAndActiveTrue(category);
        } else {
            products = productRepository.findByActiveTrue();
        }
        return products.stream().map(this::toResponse).toList();
    }

    @Override
    public ProductResponse getById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        return toResponse(product);
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
        Product product = Product.builder()
                .category(category)
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .productPrice(request.getProductPrice())
                .productQuantity(request.getProductQuantity())
                .active(true)
                .build();
        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse update(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
        product.setCategory(category);
        product.setProductName(request.getProductName());
        product.setProductDescription(request.getProductDescription());
        product.setProductPrice(request.getProductPrice());
        product.setProductQuantity(request.getProductQuantity());
        return toResponse(productRepository.save(product));
    }

    @Override
    public void delete(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        product.setActive(false);
        productRepository.save(product);
    }

    private ProductResponse toResponse(Product product) {
        Category category = product.getCategory();
        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productPrice(product.getProductPrice())
                .productQuantity(product.getProductQuantity())
                .active(product.isActive())
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .build();
    }
}
