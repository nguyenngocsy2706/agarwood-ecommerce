package com.sybanh.demo_website_tramhuong.service;

import java.util.List;

import com.sybanh.demo_website_tramhuong.dto.request.CategoryRequest;
import com.sybanh.demo_website_tramhuong.dto.response.CategoryResponse;

public interface CategoryService {
    List<CategoryResponse> getAll();

    CategoryResponse create(CategoryRequest categoryRequest);

    CategoryResponse update(Long categoryId, CategoryRequest categoryRequest);

    CategoryResponse getById(Long categoryId);

    void delete(Long categoryId);
}
