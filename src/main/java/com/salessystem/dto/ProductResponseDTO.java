package com.salessystem.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductResponseDTO {

    private Long id;
    private String name;
    private String shortDescription;
    private String category;
    private boolean variesBySize;
    private boolean variesByColor;
    private String photoUrl;
    private String ncm;
    private Long supplierId;
    private String supplierName;
    private BigDecimal cost;
    private String formattedCost;
    private BigDecimal salePrice;
    private String formattedSalePrice;
    private BigDecimal marginPercentage;
    private String formattedMargin;
    private boolean active;
    private int totalStock;
    private int totalAvailable;
    private boolean lowStock;
    private List<ProductVariantResponseDTO> variants;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isVariesBySize() {
        return variesBySize;
    }

    public void setVariesBySize(boolean variesBySize) {
        this.variesBySize = variesBySize;
    }

    public boolean isVariesByColor() {
        return variesByColor;
    }

    public void setVariesByColor(boolean variesByColor) {
        this.variesByColor = variesByColor;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getNcm() {
        return ncm;
    }

    public void setNcm(String ncm) {
        this.ncm = ncm;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getFormattedCost() {
        return formattedCost;
    }

    public void setFormattedCost(String formattedCost) {
        this.formattedCost = formattedCost;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public String getFormattedSalePrice() {
        return formattedSalePrice;
    }

    public void setFormattedSalePrice(String formattedSalePrice) {
        this.formattedSalePrice = formattedSalePrice;
    }

    public BigDecimal getMarginPercentage() {
        return marginPercentage;
    }

    public void setMarginPercentage(BigDecimal marginPercentage) {
        this.marginPercentage = marginPercentage;
    }

    public String getFormattedMargin() {
        return formattedMargin;
    }

    public void setFormattedMargin(String formattedMargin) {
        this.formattedMargin = formattedMargin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public int getTotalAvailable() {
        return totalAvailable;
    }

    public void setTotalAvailable(int totalAvailable) {
        this.totalAvailable = totalAvailable;
    }

    public boolean isLowStock() {
        return lowStock;
    }

    public void setLowStock(boolean lowStock) {
        this.lowStock = lowStock;
    }

    public List<ProductVariantResponseDTO> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantResponseDTO> variants) {
        this.variants = variants;
    }
}
