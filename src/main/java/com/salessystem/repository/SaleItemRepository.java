package com.salessystem.repository;

import com.salessystem.entity.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    boolean existsByProductVariantId(Long productVariantId);

    @Query("""
            SELECT COALESCE(SUM(si.unitCost * si.quantity), 0) FROM SaleItem si
            WHERE si.sale.saleDate BETWEEN :start AND :end AND si.sale.status = com.salessystem.entity.SaleStatus.COMPLETED
            """)
    BigDecimal sumCostBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            SELECT si.productVariant.product.id AS productId,
                   si.productVariant.product.name AS productName,
                   SUM(si.quantity) AS totalSold
            FROM SaleItem si
            WHERE si.sale.status = com.salessystem.entity.SaleStatus.COMPLETED
            GROUP BY si.productVariant.product.id, si.productVariant.product.name
            ORDER BY SUM(si.quantity) DESC
            """)
    List<TopProductProjection> findTopSellingProducts(Pageable pageable);
}
