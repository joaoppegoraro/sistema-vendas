package com.salessystem.service;

import com.salessystem.dto.DashboardSummaryDTO;
import com.salessystem.dto.TopProductDTO;
import com.salessystem.mapper.SaleMapper;
import com.salessystem.repository.ProductVariantRepository;
import com.salessystem.repository.SaleItemRepository;
import com.salessystem.repository.SaleRepository;
import com.salessystem.repository.TopProductProjection;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {

    private static final int TOP_PRODUCTS_LIMIT = 5;

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductVariantRepository variantRepository;
    private final SaleMapper saleMapper;

    public DashboardService(SaleRepository saleRepository,
                             SaleItemRepository saleItemRepository,
                             ProductVariantRepository variantRepository,
                             SaleMapper saleMapper) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.variantRepository = variantRepository;
        this.saleMapper = saleMapper;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        BigDecimal monthlyRevenue = saleRepository.sumTotalBetween(monthStart, now);
        BigDecimal monthlyCost = saleItemRepository.sumCostBetween(monthStart, now);
        BigDecimal monthlyProfit = monthlyRevenue.subtract(monthlyCost);

        // Stock value counts every variant regardless of Product.active: a discontinued
        // item still sitting on the shelf is still real capital tied up.
        BigDecimal stockValue = variantRepository.calculateStockValue();

        List<TopProductDTO> topProducts = saleItemRepository
                .findTopSellingProducts(PageRequest.of(0, TOP_PRODUCTS_LIMIT))
                .stream()
                .map(this::toTopProductDto)
                .toList();

        DashboardSummaryDTO dto = new DashboardSummaryDTO();
        dto.setFormattedMonthlyRevenue(saleMapper.formatCurrency(monthlyRevenue));
        dto.setFormattedMonthlyProfit(saleMapper.formatCurrency(monthlyProfit));
        dto.setFormattedStockValue(saleMapper.formatCurrency(stockValue));
        dto.setTopProducts(topProducts);
        return dto;
    }

    private TopProductDTO toTopProductDto(TopProductProjection projection) {
        TopProductDTO dto = new TopProductDTO();
        dto.setProductId(projection.getProductId());
        dto.setProductName(projection.getProductName());
        dto.setTotalSold(projection.getTotalSold());
        return dto;
    }
}
