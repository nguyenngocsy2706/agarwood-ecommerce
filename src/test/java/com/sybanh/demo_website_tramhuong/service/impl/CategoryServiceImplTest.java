package com.sybanh.demo_website_tramhuong.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sybanh.demo_website_tramhuong.dto.request.CategoryRequest;
import com.sybanh.demo_website_tramhuong.dto.response.CategoryResponse;
import com.sybanh.demo_website_tramhuong.entity.Category;
import com.sybanh.demo_website_tramhuong.exception.ResourceNotFoundException;
import com.sybanh.demo_website_tramhuong.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder().categoryId(1L).categoryName("Tram Huong").build();
    }

    @Test
    void getById_traVeDung_khiTonTai() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getById(1L);

        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo("Tram Huong");
    }

    @Test
    void getById_nemResourceNotFoundException_khiKhongTonTai() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAll_traVeDanhSach() {
        Category category2 = Category.builder().categoryId(2L).categoryName("Nhang Sach").build();
        when(categoryRepository.findAll()).thenReturn(List.of(category, category2));

        List<CategoryResponse> responses = categoryService.getAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCategoryName()).isEqualTo("Tram Huong");
        assertThat(responses.get(1).getCategoryName()).isEqualTo("Nhang Sach");
    }

    @Test
    void create_luuVaTraVeDung() {
        CategoryRequest request = CategoryRequest.builder().categoryName("Danh Muc Moi").build();
        // thenAnswer: trả lại CHÍNH object mà code truyền vào save(...), giả lập hành vi
        // thật của JPA khi save 1 entity mới (không tự sinh categoryId vì test không chạm DB thật)
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.create(request);

        assertThat(response.getCategoryName()).isEqualTo("Danh Muc Moi");
    }

    @Test
    void update_capNhatDung_khiTonTai() {
        CategoryRequest request = CategoryRequest.builder().categoryName("Ten Moi").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.update(1L, request);

        assertThat(response.getCategoryName()).isEqualTo("Ten Moi");
    }

    @Test
    void update_nemResourceNotFoundException_khiKhongTonTai() {
        CategoryRequest request = CategoryRequest.builder().categoryName("Ten Moi").build();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_nemResourceNotFoundException_khiKhongTonTai() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
