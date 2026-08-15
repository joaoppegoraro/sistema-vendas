package com.salessystem.service;

import com.salessystem.entity.Product;
import com.salessystem.entity.ProductVariant;
import com.salessystem.entity.Sale;
import com.salessystem.entity.SaleStatus;
import com.salessystem.repository.ProductRepository;
import com.salessystem.repository.SaleRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Plain CSV, no library: `;` delimiter and a UTF-8 BOM at the start, because that's what makes
 * Brazilian Excel split columns and render accented characters correctly without a manual import.
 */
@Service
public class CsvExportService {

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String DELIMITER = ";";
    private static final String LINE_BREAK = "\r\n";

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public CsvExportService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public void exportSales(HttpServletResponse response, LocalDate date, YearMonth month) throws IOException {
        LocalDateTime start;
        LocalDateTime end;
        if (date != null) {
            start = date.atStartOfDay();
            end = date.plusDays(1).atStartOfDay();
        } else if (month != null) {
            start = month.atDay(1).atStartOfDay();
            end = month.plusMonths(1).atDay(1).atStartOfDay();
        } else {
            YearMonth currentMonth = YearMonth.now();
            start = currentMonth.atDay(1).atStartOfDay();
            end = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        }

        PrintWriter writer = openWriter(response, "vendas");
        writer.write(row("Data", "Cliente", "CPF", "Pagamento", "Itens", "Total"));
        for (Sale sale : saleRepository.findBySaleDateBetweenAndStatusOrderBySaleDateAsc(start, end, SaleStatus.COMPLETED)) {
            String items = sale.getItems().stream()
                    .map(item -> item.getQuantity() + "x " + item.getProductVariant().getProduct().getName())
                    .collect(Collectors.joining(", "));
            writer.write(row(
                    sale.getSaleDate().format(DATE_TIME_FORMATTER),
                    sale.getClient() != null ? sale.getClient().getName() : "Sem cliente",
                    sale.getClient() != null ? formatCpf(sale.getClient().getCpf()) : "",
                    sale.getPaymentMethod().getLabel(),
                    items,
                    formatNumber(sale.getTotal())
            ));
        }
        writer.flush();
    }

    /** Always the current stock — there's no dated stock history to restate a past date accurately. */
    @Transactional(readOnly = true)
    public void exportStock(HttpServletResponse response) throws IOException {
        PrintWriter writer = openWriter(response, "estoque");
        writer.write(row("Produto", "Variação", "Categoria", "Custo Unitário", "Quantidade em Estoque", "Valor Total"));
        for (Product product : productRepository.findAll()) {
            for (ProductVariant variant : product.getVariants()) {
                BigDecimal totalValue = product.getCost().multiply(BigDecimal.valueOf(variant.getStockQuantity()));
                writer.write(row(
                        product.getName(),
                        variantLabel(variant),
                        product.getCategory(),
                        formatNumber(product.getCost()),
                        String.valueOf(variant.getStockQuantity()),
                        formatNumber(totalValue)
                ));
            }
        }
        writer.flush();
    }

    private PrintWriter openWriter(HttpServletResponse response, String baseName) throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + baseName + "-" + LocalDate.now() + ".csv\"");
        PrintWriter writer = response.getWriter();
        writer.write("\uFEFF");
        return writer;
    }

    private String row(String... values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(DELIMITER);
            }
            line.append(escape(values[i]));
        }
        return line.append(LINE_BREAK).toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(DELIMITER) || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }

    private String formatNumber(BigDecimal value) {
        if (value == null) {
            return "";
        }
        NumberFormat format = NumberFormat.getNumberInstance(PT_BR);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(value);
    }

    private String formatCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
    }

    private String variantLabel(ProductVariant variant) {
        boolean hasSize = variant.getSize() != null && !variant.getSize().isBlank();
        boolean hasColor = variant.getColor() != null && !variant.getColor().isBlank();
        if (hasSize && hasColor) {
            return variant.getSize() + " - " + variant.getColor();
        }
        if (hasSize) {
            return variant.getSize();
        }
        if (hasColor) {
            return variant.getColor();
        }
        return "Único";
    }
}
