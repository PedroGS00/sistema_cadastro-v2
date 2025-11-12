package com.sistema.cadastro.repository;

import com.sistema.cadastro.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);

    @Query(value = "SELECT * FROM products p WHERE p.sku = :sku AND p.id <> :id", nativeQuery = true)
    Optional<Product> findBySkuAndIdNot(@Param("sku") String sku, @Param("id") Long id);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategoryContainingIgnoreCase(String category, Pageable pageable);

    Page<Product> findByBrandContainingIgnoreCase(String brand, Pageable pageable);

    List<Product> findByStockLessThanEqualAndIsActiveTrue(Integer stock);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByIsActiveFalse(Pageable pageable);

    @Query(value = "SELECT * FROM products p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
           "(:category IS NULL OR LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
           "(:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:minStock IS NULL OR p.stock >= :minStock) AND " +
           "(:maxStock IS NULL OR p.stock <= :maxStock) AND " +
           "(:isActive IS NULL OR p.is_active = :isActive)",
           countQuery = "SELECT COUNT(*) FROM products p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
           "(:category IS NULL OR LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
           "(:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:minStock IS NULL OR p.stock >= :minStock) AND " +
           "(:maxStock IS NULL OR p.stock <= :maxStock) AND " +
           "(:isActive IS NULL OR p.is_active = :isActive)",
           nativeQuery = true)
    Page<Product> findByFilters(@Param("name") String name,
                               @Param("sku") String sku,
                               @Param("category") String category,
                               @Param("brand") String brand,
                               @Param("minPrice") BigDecimal minPrice,
                               @Param("maxPrice") BigDecimal maxPrice,
                               @Param("minStock") Integer minStock,
                               @Param("maxStock") Integer maxStock,
                               @Param("isActive") Boolean isActive,
                               Pageable pageable);

    @Query(value = "SELECT * FROM products p WHERE p.stock <= :lowStockThreshold AND p.is_active = TRUE", nativeQuery = true)
    List<Product> findLowStockProducts(@Param("lowStockThreshold") Integer lowStockThreshold);

    @Query(value = "SELECT COUNT(*) FROM products p WHERE p.is_active = TRUE", nativeQuery = true)
    long countActiveProducts();

    @Query(value = "SELECT COUNT(*) FROM products p WHERE p.is_active = FALSE", nativeQuery = true)
    long countInactiveProducts();

    @Query(value = "SELECT SUM(p.stock) FROM products p WHERE p.is_active = TRUE", nativeQuery = true)
    Long getTotalStock();

    @Query(value = "SELECT p.category, COUNT(*) FROM products p WHERE p.is_active = TRUE GROUP BY p.category", nativeQuery = true)
    List<Object[]> countProductsByCategory();

    @Query(value = "SELECT p.brand, COUNT(*) FROM products p WHERE p.is_active = TRUE GROUP BY p.brand", nativeQuery = true)
    List<Object[]> countProductsByBrand();
}
