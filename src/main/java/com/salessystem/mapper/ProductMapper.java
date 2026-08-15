package com.salessystem.mapper;

import com.salessystem.dto.ProductRequestDTO;
import com.salessystem.dto.ProductResponseDTO;
import com.salessystem.dto.ProductVariantResponseDTO;
import com.salessystem.entity.Product;
import com.salessystem.entity.ProductVariant;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Component
public class ProductMapper {

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final int LOW_STOCK_THRESHOLD = 1;

    public Product toEntity(ProductRequestDTO dto) {
        Product product = new Product();
        applyDto(dto, product);
        return product;
    }

    private void applyDto(ProductRequestDTO dto, Product product) {
        product.setName(dto.getName());
        product.setShortDescription(dto.getShortDescription());
        product.setCategory(dto.getCategory());
        product.setVariesBySize(dto.isVariesBySize());
        product.setVariesByColor(dto.isVariesByColor());
        product.setCost(dto.getCost());
        product.setSalePrice(dto.getSalePrice());
        product.setNcm(normalizeBlank(dto.getNcm()));
    }

    /**
     * Updates only the fields editable after creation. variesBySize/variesByColor are
     * intentionally excluded: they define the shape of the product's existing variants,
     * so changing them post-creation would desync the variant rows already on record.
     */
    public void updateBasicFields(ProductRequestDTO dto, Product product) {
        product.setName(dto.getName());
        product.setShortDescription(dto.getShortDescription());
        product.setCategory(dto.getCategory());
        product.setCost(dto.getCost());
        product.setSalePrice(dto.getSalePrice());
        product.setNcm(normalizeBlank(dto.getNcm()));
    }

    private String normalizeBlank(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public ProductRequestDTO toRequestDto(Product product) {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setShortDescription(product.getShortDescription());
        dto.setCategory(product.getCategory());
        dto.setVariesBySize(product.isVariesBySize());
        dto.setVariesByColor(product.isVariesByColor());
        dto.setCost(product.getCost());
        dto.setSalePrice(product.getSalePrice());
        dto.setNcm(product.getNcm());
        dto.setSupplierId(product.getSupplier() == null ? null : product.getSupplier().getId());
        return dto;
    }

    public ProductResponseDTO toResponseDto(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setShortDescription(product.getShortDescription());
        dto.setCategory(product.getCategory());
        dto.setVariesBySize(product.isVariesBySize());
        dto.setVariesByColor(product.isVariesByColor());
        dto.setPhotoUrl(product.getPhotoData() == null ? null : "/products/" + product.getId() + "/photo");
        dto.setNcm(product.getNcm());
        if (product.getSupplier() != null) {
            dto.setSupplierId(product.getSupplier().getId());
            dto.setSupplierName(product.getSupplier().getName());
        }
        dto.setCost(product.getCost());
        dto.setFormattedCost(formatCurrency(product.getCost()));
        dto.setSalePrice(product.getSalePrice());
        dto.setFormattedSalePrice(formatCurrency(product.getSalePrice()));
        BigDecimal margin = calculateMarginPercentage(product.getCost(), product.getSalePrice());
        dto.setMarginPercentage(margin);
        dto.setFormattedMargin(margin == null ? "-" : margin + "%");
        dto.setActive(product.isActive());

        List<ProductVariantResponseDTO> variantDtos = product.getVariants().stream()
                .map(this::toVariantResponseDto)
                .toList();
        dto.setVariants(variantDtos);

        int totalStock = variantDtos.stream().mapToInt(ProductVariantResponseDTO::getStockQuantity).sum();
        int totalAvailable = variantDtos.stream().mapToInt(ProductVariantResponseDTO::getAvailableQuantity).sum();
        dto.setTotalStock(totalStock);
        dto.setTotalAvailable(totalAvailable);
        dto.setLowStock(totalAvailable <= LOW_STOCK_THRESHOLD);

        return dto;
    }

    public ProductVariantResponseDTO toVariantResponseDto(ProductVariant variant) {
        ProductVariantResponseDTO dto = new ProductVariantResponseDTO();
        dto.setId(variant.getId());
        dto.setProductId(variant.getProduct().getId());
        dto.setProductName(variant.getProduct().getName());
        dto.setSize(variant.getSize());
        dto.setColor(variant.getColor());
        dto.setLabel(buildVariantLabel(variant.getSize(), variant.getColor()));
        dto.setStockQuantity(variant.getStockQuantity());
        dto.setReservedQuantity(variant.getReservedQuantity());
        dto.setAvailableQuantity(variant.getAvailableQuantity());
        dto.setLowStock(variant.getAvailableQuantity() <= LOW_STOCK_THRESHOLD);
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

    private BigDecimal calculateMarginPercentage(BigDecimal cost, BigDecimal salePrice) {
        if (cost == null || salePrice == null || salePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return salePrice.subtract(cost)
                .divide(salePrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return NumberFormat.getCurrencyInstance(PT_BR).format(value);
    }
}
