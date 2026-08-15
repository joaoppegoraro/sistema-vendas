package com.salessystem.mapper;

import com.salessystem.dto.SaleItemResponseDTO;
import com.salessystem.dto.SaleResponseDTO;
import com.salessystem.entity.Sale;
import com.salessystem.entity.SaleItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class SaleMapper {

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public SaleResponseDTO toResponseDto(Sale sale) {
        SaleResponseDTO dto = new SaleResponseDTO();
        dto.setId(sale.getId());
        if (sale.getClient() != null) {
            dto.setClientId(sale.getClient().getId());
            dto.setClientName(sale.getClient().getName());
            dto.setClientCpf(formatCpf(sale.getClient().getCpf()));
        }
        dto.setSaleDate(sale.getSaleDate());
        dto.setFormattedSaleDate(sale.getSaleDate().format(DATE_TIME_FORMATTER));
        dto.setFormattedDiscount(formatCurrency(sale.getDiscount()));
        dto.setFormattedSurcharge(formatCurrency(sale.getSurcharge()));
        dto.setPaymentMethodLabel(sale.getPaymentMethod().getLabel());
        dto.setStatus(sale.getStatus());
        dto.setStatusLabel(sale.getStatus().getLabel());
        dto.setFormattedTotal(formatCurrency(sale.getTotal()));

        List<SaleItemResponseDTO> items = sale.getItems().stream().map(this::toItemResponseDto).toList();
        dto.setItems(items);

        return dto;
    }

    private SaleItemResponseDTO toItemResponseDto(SaleItem item) {
        SaleItemResponseDTO dto = new SaleItemResponseDTO();
        dto.setProductName(item.getProductVariant().getProduct().getName());
        dto.setVariantLabel(buildVariantLabel(item.getProductVariant().getSize(), item.getProductVariant().getColor()));
        dto.setNcm(item.getProductVariant().getProduct().getNcm());
        dto.setQuantity(item.getQuantity());
        dto.setFormattedUnitPrice(formatCurrency(item.getUnitPrice()));
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        dto.setFormattedSubtotal(formatCurrency(subtotal));
        return dto;
    }

    private String buildVariantLabel(String size, String color) {
        boolean hasSize = size != null && !size.isBlank();
        boolean hasColor = color != null && !color.isBlank();
        if (hasSize && hasColor) {
            return size + " - " + color;
        }
        if (hasSize) {
            return size;
        }
        if (hasColor) {
            return color;
        }
        return "Único";
    }

    private String formatCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
    }

    public String formatCurrency(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return NumberFormat.getCurrencyInstance(PT_BR).format(value);
    }
}
