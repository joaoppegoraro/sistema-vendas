package com.salessystem.service;

import com.salessystem.dto.SaleResponseDTO;
import com.salessystem.dto.report.AnnualRevenueDTO;
import com.salessystem.dto.report.ClientAnalyticsDTO;
import com.salessystem.dto.report.ClientPurchaseSummaryDTO;
import com.salessystem.dto.report.DailyCashClosingDTO;
import com.salessystem.dto.report.MonthlyRevenueDTO;
import com.salessystem.dto.report.PaymentTotalLineDTO;
import com.salessystem.dto.report.ProfitabilityReportDTO;
import com.salessystem.entity.PaymentMethod;
import com.salessystem.entity.SaleStatus;
import com.salessystem.mapper.SaleMapper;
import com.salessystem.repository.ClientPurchaseProjection;
import com.salessystem.repository.PaymentTotalProjection;
import com.salessystem.repository.SaleItemRepository;
import com.salessystem.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReportService {

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int TOP_CLIENTS_LIMIT = 10;
    private static final Map<PaymentMethod, String> CHECK_HINTS = Map.of(
            PaymentMethod.PIX, "Confira no app do banco",
            PaymentMethod.CREDIT_CARD, "Confira na maquininha",
            PaymentMethod.DEBIT_CARD, "Confira na maquininha",
            PaymentMethod.CASH, "Confira a gaveta"
    );

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final SaleMapper saleMapper;

    public ReportService(SaleRepository saleRepository, SaleItemRepository saleItemRepository, SaleMapper saleMapper) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.saleMapper = saleMapper;
    }

    /** Runs the same sumTotalBetween used by the dashboard once per calendar month — no new query needed. */
    @Transactional(readOnly = true)
    public AnnualRevenueDTO getAnnualRevenue(int year) {
        List<MonthlyRevenueDTO> months = new ArrayList<>();
        BigDecimal annualTotal = BigDecimal.ZERO;
        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            BigDecimal monthTotal = saleRepository.sumTotalBetween(startOfMonth(yearMonth), startOfMonth(yearMonth.plusMonths(1)));
            annualTotal = annualTotal.add(monthTotal);

            MonthlyRevenueDTO monthDto = new MonthlyRevenueDTO();
            monthDto.setMonthLabel(capitalize(Month.of(month).getDisplayName(TextStyle.FULL, PT_BR)));
            monthDto.setFormattedTotal(saleMapper.formatCurrency(monthTotal));
            months.add(monthDto);
        }

        AnnualRevenueDTO dto = new AnnualRevenueDTO();
        dto.setYear(year);
        dto.setMonths(months);
        dto.setFormattedAnnualTotal(saleMapper.formatCurrency(annualTotal));
        return dto;
    }

    /** Completed sales in the period, formatted with client CPF and item NCM for invoice prep. */
    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getInvoiceMirror(LocalDate date, YearMonth month) {
        LocalDateTime start;
        LocalDateTime end;
        if (date != null) {
            start = date.atStartOfDay();
            end = date.plusDays(1).atStartOfDay();
        } else if (month != null) {
            start = startOfMonth(month);
            end = startOfMonth(month.plusMonths(1));
        } else {
            YearMonth currentMonth = YearMonth.now();
            start = startOfMonth(currentMonth);
            end = startOfMonth(currentMonth.plusMonths(1));
        }
        return saleRepository.findBySaleDateBetweenAndStatusOrderBySaleDateAsc(start, end, SaleStatus.COMPLETED)
                .stream()
                .map(saleMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DailyCashClosingDTO getDailyCashClosing(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        Map<PaymentMethod, BigDecimal> totalsByMethod = new EnumMap<>(PaymentMethod.class);
        for (PaymentMethod method : PaymentMethod.values()) {
            totalsByMethod.put(method, BigDecimal.ZERO);
        }
        for (PaymentTotalProjection projection : saleRepository.sumTotalByPaymentMethodBetween(start, end)) {
            totalsByMethod.put(projection.getPaymentMethod(), projection.getTotal());
        }

        List<PaymentTotalLineDTO> lines = new ArrayList<>();
        BigDecimal dayTotal = BigDecimal.ZERO;
        for (PaymentMethod method : PaymentMethod.values()) {
            BigDecimal methodTotal = totalsByMethod.get(method);
            dayTotal = dayTotal.add(methodTotal);

            PaymentTotalLineDTO line = new PaymentTotalLineDTO();
            line.setPaymentMethodLabel(method.getLabel());
            line.setFormattedTotal(saleMapper.formatCurrency(methodTotal));
            line.setCheckHint(CHECK_HINTS.get(method));
            lines.add(line);
        }

        DailyCashClosingDTO dto = new DailyCashClosingDTO();
        dto.setDate(date);
        dto.setFormattedDate(date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dto.setFormattedTotal(saleMapper.formatCurrency(dayTotal));
        dto.setLines(lines);
        return dto;
    }

    /** Same revenue-minus-cost math as the dashboard's monthly profit card, for any chosen month. */
    @Transactional(readOnly = true)
    public ProfitabilityReportDTO getProfitabilityReport(YearMonth month) {
        LocalDateTime start = startOfMonth(month);
        LocalDateTime end = startOfMonth(month.plusMonths(1));

        BigDecimal revenue = saleRepository.sumTotalBetween(start, end);
        BigDecimal cost = saleItemRepository.sumCostBetween(start, end);
        BigDecimal profit = revenue.subtract(cost);

        ProfitabilityReportDTO dto = new ProfitabilityReportDTO();
        dto.setMonthLabel(capitalize(month.getMonth().getDisplayName(TextStyle.FULL, PT_BR)) + "/" + month.getYear());
        dto.setFormattedRevenue(saleMapper.formatCurrency(revenue));
        dto.setFormattedCost(saleMapper.formatCurrency(cost));
        dto.setFormattedProfit(saleMapper.formatCurrency(profit));
        return dto;
    }

    /**
     * One query, three cuts of the same data: who spends the most, who used to buy repeatedly
     * and stopped, and who bought exactly once and never came back. "Stopped"/"never came back"
     * both require inactiveDays to have passed since the last purchase, so a client who bought
     * last week isn't flagged just for not having bought again yet.
     */
    @Transactional(readOnly = true)
    public ClientAnalyticsDTO getClientAnalytics(int inactiveDays) {
        List<ClientPurchaseSummaryDTO> all = saleRepository.summarizeByClient().stream()
                .map(this::toSummaryDto)
                .toList();

        List<ClientPurchaseSummaryDTO> top = all.stream()
                .sorted(Comparator.comparing(ClientPurchaseSummaryDTO::getTotalSpent).reversed())
                .limit(TOP_CLIENTS_LIMIT)
                .toList();

        List<ClientPurchaseSummaryDTO> churned = all.stream()
                .filter(c -> c.getPurchaseCount() >= 2 && c.getDaysSinceLastPurchase() >= inactiveDays)
                .sorted(Comparator.comparingLong(ClientPurchaseSummaryDTO::getDaysSinceLastPurchase).reversed())
                .toList();

        List<ClientPurchaseSummaryDTO> oneTime = all.stream()
                .filter(c -> c.getPurchaseCount() == 1 && c.getDaysSinceLastPurchase() >= inactiveDays)
                .sorted(Comparator.comparingLong(ClientPurchaseSummaryDTO::getDaysSinceLastPurchase).reversed())
                .toList();

        ClientAnalyticsDTO dto = new ClientAnalyticsDTO();
        dto.setInactiveDays(inactiveDays);
        dto.setTopClients(top);
        dto.setChurnedClients(churned);
        dto.setOneTimeClients(oneTime);
        return dto;
    }

    private ClientPurchaseSummaryDTO toSummaryDto(ClientPurchaseProjection projection) {
        ClientPurchaseSummaryDTO dto = new ClientPurchaseSummaryDTO();
        dto.setClientId(projection.getClientId());
        dto.setClientName(projection.getClientName());
        dto.setWhatsAppUrl(buildWhatsAppUrl(projection.getClientPhone()));
        dto.setTotalSpent(projection.getTotalSpent());
        dto.setFormattedTotalSpent(saleMapper.formatCurrency(projection.getTotalSpent()));
        dto.setPurchaseCount(projection.getPurchaseCount());
        dto.setFormattedLastPurchaseDate(projection.getLastPurchaseDate().format(DATE_TIME_FORMATTER));
        dto.setDaysSinceLastPurchase(ChronoUnit.DAYS.between(projection.getLastPurchaseDate().toLocalDate(), LocalDate.now()));
        return dto;
    }

    private String buildWhatsAppUrl(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : "https://wa.me/55" + digits;
    }

    private LocalDateTime startOfMonth(YearMonth yearMonth) {
        return yearMonth.atDay(1).atStartOfDay();
    }

    private String capitalize(String value) {
        return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
