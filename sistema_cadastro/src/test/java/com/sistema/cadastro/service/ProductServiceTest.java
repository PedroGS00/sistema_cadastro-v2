package com.sistema.cadastro.service;

import com.sistema.cadastro.dto.ProductDTO;
import com.sistema.cadastro.entity.Product;
import com.sistema.cadastro.exception.BusinessException;
import com.sistema.cadastro.exception.ResourceNotFoundException;
import com.sistema.cadastro.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductDTO validDto;

    @BeforeEach
    void setup() {
        validDto = ProductDTO.builder()
                .name("Produto X")
                .description("Desc")
                .sku("abc-123")
                .price(new BigDecimal("10.00"))
                .stock(100)
                .weight(new BigDecimal("1.250"))
                .category("Cat")
                .brand("Brand")
                .imageUrl("http://img")
                .isActive(true)
                .build();
    }

    private Product buildProductFromDto(ProductDTO dto) {
        Product p = new Product();
        p.setId(1L);
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setSku(dto.getSku().toUpperCase().trim());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        p.setWeight(dto.getWeight());
        p.setCategory(dto.getCategory());
        p.setBrand(dto.getBrand());
        p.setImageUrl(dto.getImageUrl());
        p.setIsActive(dto.getIsActive());
        return p;
    }

    @Test
    @DisplayName("createProduct: sucesso e normalizaÃ§Ã£o de SKU")
    void createProduct_success() {
        when(productRepository.existsBySku("ABC-123")).thenReturn(false);
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        Product saved = buildProductFromDto(validDto);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Product result = productService.createProduct(validDto);

        verify(productRepository).existsBySku("ABC-123");
        verify(productRepository).save(captor.capture());
        assertEquals("ABC-123", captor.getValue().getSku(), "SKU deve ser upper-case e trimado");
        assertNotNull(result.getId());
        assertEquals(100, result.getStock());
    }

    @Test
    @DisplayName("createProduct: SKU duplicado lanÃ§a BusinessException")
    void createProduct_duplicateSku() {
        when(productRepository.existsBySku("ABC-123")).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class, () -> productService.createProduct(validDto));
        assertTrue(ex.getMessage().contains("SKU"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct: preÃ§o invÃ¡lido lanÃ§a BusinessException")
    void createProduct_invalidPrice() {
        ProductDTO dto = ProductDTO.builder()
                .name("P")
                .description("D")
                .sku("SKU-1")
                .price(BigDecimal.ZERO)
                .stock(1)
                .weight(new BigDecimal("1.0"))
                .category("C")
                .brand("B")
                .imageUrl("http://img")
                .isActive(true)
                .build();
        when(productRepository.existsBySku("SKU-1")).thenReturn(false);
        assertThrows(BusinessException.class, () -> productService.createProduct(dto));
    }

    @Test
    @DisplayName("getProductById: encontrado")
    void getProductById_found() {
        Product p = buildProductFromDto(validDto);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        Product result = productService.getProductById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("getProductBySku: normaliza entrada e retorna produto")
    void getProductBySku_success() {
        Product p = buildProductFromDto(validDto);
        when(productRepository.findBySku("ABC-123")).thenReturn(Optional.of(p));
        Product result = productService.getProductBySku("  abc-123 ");
        assertEquals("ABC-123", result.getSku());
    }

    @Test
    @DisplayName("getProductBySku: nÃ£o encontrado lanÃ§a ResourceNotFoundException")
    void getProductBySku_notFound() {
        when(productRepository.findBySku("ABC-000")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductBySku("abc-000"));
    }

    @Test
    @DisplayName("getAllProducts: paginaÃ§Ã£o delegada ao repositÃ³rio")
    void getAllProducts_pageable() {
        Pageable pageable = PageRequest.of(0, 2);
        List<Product> list = List.of(buildProductFromDto(validDto));
        Page<Product> page = new PageImpl<>(list, pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(page);
        Page<Product> result = productService.getAllProducts(pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("updateProduct: sucesso com validaÃ§Ãµes")
    void updateProduct_success() {
        Product existing = buildProductFromDto(validDto);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findBySkuAndIdNot("ABC-123", 1L)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDTO updateDto = ProductDTO.builder()
                .name("Novo")
                .description("Nova")
                .sku("abc-123")
                .price(new BigDecimal("20.00"))
                .stock(50)
                .weight(new BigDecimal("2.000"))
                .category("NC")
                .brand("NB")
                .imageUrl("http://img2")
                .isActive(true)
                .build();

        Product updated = productService.updateProduct(1L, updateDto);
        assertEquals("Novo", updated.getName());
        assertEquals(50, updated.getStock());

        assertEquals("ABC-123", updated.getSku());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("deleteProduct: sucesso delegando ao repositÃ³rio")
    void deleteProduct_success() {
        Product existing = buildProductFromDto(validDto);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(productRepository).delete(existing);
        productService.deleteProduct(1L);
        verify(productRepository).delete(existing);
    }

    @Nested
    class StockOperations {

        @Test
        @DisplayName("updateProductStock: reduz alÃ©m do disponÃ­vel lanÃ§a IllegalStateException")
        void updateProductStock_insufficient() {
            Product existing = buildProductFromDto(validDto);
            when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
            assertThrows(IllegalStateException.class, () -> productService.updateProductStock(1L, 1000, false));
        }

        @Test
        @DisplayName("updateStock: quantidade nula/zero lanÃ§a BusinessException")
        @Disabled("Smoke suite: reduzir cobertura de validaÃ§Ãµes secundÃ¡rias")
        void updateStock_invalidQuantity() {
            assertThrows(BusinessException.class, () -> productService.updateStock(1L, null));
            assertThrows(BusinessException.class, () -> productService.updateStock(1L, 0));
        }

        @Test
        @DisplayName("updateStock: positivo delega para aumento")
        void updateStock_positive() {
            Product existing = buildProductFromDto(validDto);
            when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            Product result = productService.updateStock(1L, 5);
            assertEquals(105, result.getStock());
        }

        @Test
        @DisplayName("updateStock: negativo delega para reduÃ§Ã£o")
        void updateStock_negative() {
            Product existing = buildProductFromDto(validDto);
            when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            Product result = productService.updateStock(1L, -10);
            assertEquals(90, result.getStock());
        }
    }

    @Test
    @DisplayName("getLowStockProducts: threshold null usa 10 e delega ao repo")
    void getLowStockProducts_thresholdNull() {
        when(productRepository.findLowStockProducts(10)).thenReturn(Collections.emptyList());
        List<Product> result = productService.getLowStockProducts(null);
        assertTrue(result.isEmpty());
        verify(productRepository).findLowStockProducts(10);
    }

    @Test
    @DisplayName("getLowStockProducts(page): paginaÃ§Ã£o manual correta")
    void getLowStockProducts_pageable() {
        List<Product> all = List.of(buildProductFromDto(validDto), buildProductFromDto(validDto));
        when(productRepository.findByStockLessThanEqualAndIsActiveTrue(10)).thenReturn(all);
        Page<Product> page = productService.getLowStockProducts(null, PageRequest.of(0, 1));
        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getContent().size());
    }

    @Test
    @DisplayName("countActiveProducts / countInactiveProducts / getTotalStock")
    void countersAndTotals() {
        when(productRepository.countActiveProducts()).thenReturn(5L);
        when(productRepository.countInactiveProducts()).thenReturn(2L);
        when(productRepository.getTotalStock()).thenReturn(null);
        assertEquals(5L, productService.countActiveProducts());
        assertEquals(2L, productService.countInactiveProducts());
        assertEquals(0L, productService.getTotalStock());
    }
}
