package com.sybanh.demo_website_tramhuong.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sybanh.demo_website_tramhuong.dto.request.CategoryRequest;
import com.sybanh.demo_website_tramhuong.dto.response.CategoryResponse;
import com.sybanh.demo_website_tramhuong.entity.Category;
import com.sybanh.demo_website_tramhuong.exception.ResourceNotFoundException;
import com.sybanh.demo_website_tramhuong.repository.CategoryRepository;
import com.sybanh.demo_website_tramhuong.service.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse create(CategoryRequest categoryRequest) {
        Category category = Category.builder()
                .categoryName(categoryRequest.getCategoryName())
                .build();
        return toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse update(Long categoryId, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        category.setCategoryName(categoryRequest.getCategoryName());
        return toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse getById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        return toResponse(category);
    }

    @Override
    public void delete(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .build();
    }
}
