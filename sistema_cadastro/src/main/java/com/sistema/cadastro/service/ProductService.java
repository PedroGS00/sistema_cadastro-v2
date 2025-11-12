package com.sistema.cadastro.service;

import com.sistema.cadastro.dto.ProductDTO;
import com.sistema.cadastro.entity.Product;
import com.sistema.cadastro.exception.BusinessException;
import com.sistema.cadastro.exception.ResourceNotFoundException;
import com.sistema.cadastro.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        log.info("Criando novo produto: SKU {}", productDTO.getSku());

        validateProductCreation(productDTO);

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setSku(productDTO.getSku().toUpperCase().trim());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setWeight(productDTO.getWeight());
        product.setCategory(productDTO.getCategory());
        product.setBrand(productDTO.getBrand());
        product.setImageUrl(productDTO.getImageUrl());
        product.setIsActive(productDTO.getIsActive() != null ? productDTO.getIsActive() : true);
        
        Product savedProduct = productRepository.save(product);
        log.info("Produto criado com sucesso: ID {}, SKU {}", savedProduct.getId(), savedProduct.getSku());
        
        return savedProduct;
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        log.info("Buscando produto por ID: {}", id);
        
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
    }

    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        log.info("Buscando produto por SKU: {}", sku);
        
        String cleanSku = sku.toUpperCase().trim();
        
        return productRepository.findBySku(cleanSku)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "SKU", sku));
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        log.info("Listando produtos com paginaÃ§Ã£o: pÃ¡gina {}, tamanho {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getActiveProducts(Pageable pageable) {
        log.info("Listando produtos ativos com paginaÃ§Ã£o");
        
        return productRepository.findByIsActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByFilters(String name, String sku, String category, String brand,
                                            BigDecimal minPrice, BigDecimal maxPrice,
                                            Integer minStock, Integer maxStock,
                                            Boolean isActive, Pageable pageable) {
        log.info("Buscando produtos com filtros: nome={}, sku={}, categoria={}, marca={}, " +
                "minPrice={}, maxPrice={}, minStock={}, maxStock={}, isActive={}",
                name, sku, category, brand, minPrice, maxPrice, minStock, maxStock, isActive);
        
        return productRepository.findByFilters(name, sku, category, brand, minPrice, maxPrice,
                minStock, maxStock, isActive, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByName(String name, Pageable pageable) {
        log.info("Buscando produtos por nome: {}", name);
        
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        log.info("Buscando produtos por categoria: {}", category);
        
        return productRepository.findByCategoryContainingIgnoreCase(category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByBrand(String brand, Pageable pageable) {
        log.info("Buscando produtos por marca: {}", brand);
        
        return productRepository.findByBrandContainingIgnoreCase(brand, pageable);
    }

    @Transactional
    public Product updateProduct(Long id, ProductDTO productDTO) {
        log.info("Atualizando produto ID: {}", id);
        
        Product existingProduct = getProductById(id);

        validateProductUpdate(id, productDTO);

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        existingProduct.setWeight(productDTO.getWeight());
        existingProduct.setCategory(productDTO.getCategory());
        existingProduct.setBrand(productDTO.getBrand());
        existingProduct.setImageUrl(productDTO.getImageUrl());
        existingProduct.setIsActive(productDTO.getIsActive() != null ? productDTO.getIsActive() : existingProduct.getIsActive());
        
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Produto atualizado com sucesso: ID {}", updatedProduct.getId());
        
        return updatedProduct;
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deletando produto ID: {}", id);
        
        Product product = getProductById(id);
        productRepository.delete(product);
        
        log.info("Produto deletado com sucesso: ID {}", id);
    }

    @Transactional
    public Product updateProductStock(Long id, Integer quantity, boolean increase) {
        log.info("Atualizando estoque do produto ID: {}, quantidade: {}, aumentar: {}", id, quantity, increase);
        
        Product product = getProductById(id);
        
        if (increase) {
            product.increaseStock(quantity);
        } else {
            product.decreaseStock(quantity);
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Estoque atualizado com sucesso: ID {}, novo estoque: {}", updatedProduct.getId(), updatedProduct.getStock());
        
        return updatedProduct;
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        log.info("Buscando produtos com estoque baixo (limite: {})", threshold);
        
        int lowStockThreshold = threshold != null ? threshold : 10;
        return productRepository.findLowStockProducts(lowStockThreshold);
    }

    @Transactional(readOnly = true)
    public Page<Product> getLowStockProducts(Integer threshold, Pageable pageable) {
        log.info("Buscando produtos com baixo estoque paginado (limite: {}), pÃ¡gina {} tamanho {}",
                threshold, pageable.getPageNumber(), pageable.getPageSize());

        int lowStockThreshold = threshold != null ? threshold : 10;
        List<Product> all = productRepository.findByStockLessThanEqualAndIsActiveTrue(lowStockThreshold);

        int start = (int) pageable.getOffset();
        if (start >= all.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, all.size());
        }
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<Product> sub = all.subList(start, end);
        return new PageImpl<>(sub, pageable, all.size());
    }

    @Transactional(readOnly = true)
    public long countActiveProducts() {
        return productRepository.countActiveProducts();
    }

    @Transactional(readOnly = true)
    public long countInactiveProducts() {
        return productRepository.countInactiveProducts();
    }

    @Transactional(readOnly = true)
    public long getTotalStock() {
        Long totalStock = productRepository.getTotalStock();
        return totalStock != null ? totalStock : 0L;
    }

    private void validateProductCreation(ProductDTO productDTO) {

        String cleanSku = productDTO.getSku().toUpperCase().trim();
        if (productRepository.existsBySku(cleanSku)) {
            throw new BusinessException("SKU jÃ¡ cadastrado", org.springframework.http.HttpStatus.CONFLICT, "DUPLICATE_SKU");
        }

        if (productDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("PreÃ§o deve ser maior que zero");
        }
        
        if (productDTO.getStock() < 0) {
            throw new BusinessException("Estoque nÃ£o pode ser negativo");
        }
        
        if (productDTO.getWeight() != null && productDTO.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Peso deve ser maior que zero");
        }
    }

    private void validateProductUpdate(Long id, ProductDTO productDTO) {

        String cleanSku = productDTO.getSku().toUpperCase().trim();
        if (productRepository.findBySkuAndIdNot(cleanSku, id).isPresent()) {
            throw new BusinessException("SKU jÃ¡ cadastrado para outro produto", org.springframework.http.HttpStatus.CONFLICT, "DUPLICATE_SKU");
        }

        if (productDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("PreÃ§o deve ser maior que zero");
        }
        
        if (productDTO.getStock() < 0) {
            throw new BusinessException("Estoque nÃ£o pode ser negativo");
        }
        
        if (productDTO.getWeight() != null && productDTO.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Peso deve ser maior que zero");
        }
    }

    @Transactional
    public Product updateStock(Long id, Integer quantity) {
        log.info("RequisiÃ§Ã£o de atualizaÃ§Ã£o de estoque: id={}, quantity={}", id, quantity);
        if (quantity == null || quantity == 0) {
            throw new BusinessException("Quantidade invÃ¡lida");
        }
        boolean increase = quantity > 0;
        int abs = Math.abs(quantity);
        return updateProductStock(id, abs, increase);
    }

    @Transactional(readOnly = true)
    public long countProductsByCategory(String category) {
        log.info("Contando produtos ativos por categoria: {}", category);
        List<Object[]> aggregates = productRepository.countProductsByCategory();
        for (Object[] row : aggregates) {
            String cat = (String) row[0];
            Long count = (Long) row[1];
            if (cat != null && cat.equalsIgnoreCase(category)) {
                return count != null ? count : 0L;
            }
        }
        return 0L;
    }

    @Transactional(readOnly = true)
    public long countProductsByBrand(String brand) {
        log.info("Contando produtos ativos por marca: {}", brand);
        List<Object[]> aggregates = productRepository.countProductsByBrand();
        for (Object[] row : aggregates) {
            String br = (String) row[0];
            Long count = (Long) row[1];
            if (br != null && br.equalsIgnoreCase(brand)) {
                return count != null ? count : 0L;
            }
        }
        return 0L;
    }
}
