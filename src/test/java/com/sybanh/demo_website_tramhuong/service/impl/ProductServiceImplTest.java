package com.sybanh.demo_website_tramhuong.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sybanh.demo_website_tramhuong.dto.request.ProductRequest;
import com.sybanh.demo_website_tramhuong.dto.response.ProductResponse;
import com.sybanh.demo_website_tramhuong.entity.Category;
import com.sybanh.demo_website_tramhuong.entity.Product;
import com.sybanh.demo_website_tramhuong.exception.ResourceNotFoundException;
import com.sybanh.demo_website_tramhuong.repository.CategoryRepository;
import com.sybanh.demo_website_tramhuong.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = Category.builder().categoryId(1L).categoryName("Tram Huong").build();
        product = Product.builder()
                .productId(10L)
                .productName("Tram Huong A")
                .productDescription("Nhang tram cao cap")
                .productPrice(BigDecimal.valueOf(50000))
                .productQuantity(5)
                .active(true)
                .category(category)
                .build();
    }

    @Test
    void getById_traVeDung_khiTonTai() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getById(10L);

        assertThat(response.getProductName()).isEqualTo("Tram Huong A");
        assertThat(response.getCategoryId()).isEqualTo(1L);
    }

    @Test
    void getById_nemResourceNotFoundException_khiKhongTonTai() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAll_khongCoCategoryId_traVeSanPhamDangActive() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.getAll(null);

        assertThat(responses).hasSize(1);
    }

    @Test
    void getAll_coCategoryId_locTheoCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByCategoryAndActiveTrue(category)).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.getAll(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCategoryId()).isEqualTo(1L);
    }

    @Test
    void getAll_nemResourceNotFoundException_khiCategoryKhongTonTai() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getAll(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_thanhCong_khiCategoryTonTai() {
        ProductRequest request = ProductRequest.builder()
                .productName("San Pham Moi")
                .productDescription("Mo ta")
                .productPrice(BigDecimal.valueOf(100000))
                .productQuantity(10)
                .categoryId(1L)
                .build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.create(request);

        assertThat(response.getProductName()).isEqualTo("San Pham Moi");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void create_nemResourceNotFoundException_khiCategoryKhongTonTai() {
        ProductRequest request = ProductRequest.builder()
                .productName("San Pham Moi")
                .productDescription("Mo ta")
                .productPrice(BigDecimal.valueOf(100000))
                .productQuantity(10)
                .categoryId(99L)
                .build();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_nemResourceNotFoundException_khiProductKhongTonTai() {
        ProductRequest request = ProductRequest.builder()
                .productName("Ten Moi")
                .productDescription("Mo ta")
                .productPrice(BigDecimal.valueOf(100000))
                .productQuantity(10)
                .categoryId(1L)
                .build();
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_setActiveFalse_khiTonTai() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        productService.delete(10L);

        // verify + argThat: kiểm tra save() được gọi với đúng 1 Product có active = false
        // (khác với when(...) là dặn mock trả lời gì, verify là kiểm tra mock ĐÃ được gọi thế nào)
        verify(productRepository).save(argThat(p -> !p.isActive()));
    }

    @Test
    void delete_nemResourceNotFoundException_khiKhongTonTai() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
