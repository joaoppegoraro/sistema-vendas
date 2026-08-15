package com.salessystem.repository;

import com.salessystem.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySupplierIdOrderByName(Long supplierId);

    @Query("""
            SELECT DISTINCT p FROM Product p
            WHERE (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:category IS NULL OR p.category = :category)
            AND (:lowStockOnly = false OR EXISTS (
                 SELECT 1 FROM ProductVariant v
                 WHERE v.product = p AND (v.stockQuantity - v.reservedQuantity) <= 1))
            """)
    Page<Product> search(@Param("search") String search,
                          @Param("category") String category,
                          @Param("lowStockOnly") boolean lowStockOnly,
                          Pageable pageable);
}
