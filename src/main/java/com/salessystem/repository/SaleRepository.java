package com.salessystem.repository;

import com.salessystem.entity.Sale;
import com.salessystem.entity.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    Page<Sale> findByClientIdOrderBySaleDateDesc(Long clientId, Pageable pageable);

    Page<Sale> findAllByOrderBySaleDateDesc(Pageable pageable);

    Page<Sale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /** Unpaginated, chronological — for reports (invoice mirror, CSV export) covering a whole period at once. */
    List<Sale> findBySaleDateBetweenAndStatusOrderBySaleDateAsc(LocalDateTime start, LocalDateTime end, SaleStatus status);

    @Query("SELECT COALESCE(SUM(s.total), 0) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end AND s.status = com.salessystem.entity.SaleStatus.COMPLETED")
    BigDecimal sumTotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            SELECT s.paymentMethod AS paymentMethod, COALESCE(SUM(s.total), 0) AS total
            FROM Sale s
            WHERE s.saleDate BETWEEN :start AND :end AND s.status = com.salessystem.entity.SaleStatus.COMPLETED
            GROUP BY s.paymentMethod
            """)
    List<PaymentTotalProjection> sumTotalByPaymentMethodBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            SELECT s.client.id AS clientId, s.client.name AS clientName, s.client.phone AS clientPhone,
                   COALESCE(SUM(s.total), 0) AS totalSpent, COUNT(s) AS purchaseCount, MAX(s.saleDate) AS lastPurchaseDate
            FROM Sale s
            WHERE s.status = com.salessystem.entity.SaleStatus.COMPLETED AND s.client IS NOT NULL
            GROUP BY s.client.id, s.client.name, s.client.phone
            """)
    List<ClientPurchaseProjection> summarizeByClient();
}
