package com.salessystem.repository;

import com.salessystem.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductId(Long productId);

    Optional<ProductVariant> findByProductIdAndSizeAndColor(Long productId, String size, String color);

    boolean existsByProductIdAndSizeAndColor(Long productId, String size, String color);

    @Query("SELECT COALESCE(SUM(v.stockQuantity * v.product.cost), 0) FROM ProductVariant v")
    BigDecimal calculateStockValue();

    @Query("""
            SELECT v FROM ProductVariant v
            WHERE v.product.active = true AND (v.stockQuantity - v.reservedQuantity) > 0
            ORDER BY v.product.name, v.size, v.color
            """)
    List<ProductVariant> findSellable();
}
