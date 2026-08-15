package com.salessystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Payload used for both creating and editing a product via the web form.
 * The id is only populated when prefilling the edit form; it is ignored on create.
 * initialStock is only meaningful when the product has no size/color variation.
 */
public class ProductRequestDTO {

    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
    private String name;

    @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres")
    private String shortDescription;

    @NotBlank(message = "A categoria é obrigatória")
    private String category;

    private boolean variesBySize;

    private boolean variesByColor;

    @NotNull(message = "O custo é obrigatório")
    @DecimalMin(value = "0.0", message = "O custo não pode ser negativo")
    private BigDecimal cost;

    @NotNull(message = "O preço de venda é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço de venda deve ser maior que zero")
    private BigDecimal salePrice;

    @Min(value = 0, message = "O estoque inicial não pode ser negativo")
    private int initialStock;

    @Pattern(regexp = "^$|^\\d{8}$", message = "Informe um NCM válido (8 dígitos)")
    private String ncm;

    private Long supplierId;

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

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public int getInitialStock() {
        return initialStock;
    }

    public void setInitialStock(int initialStock) {
        this.initialStock = initialStock;
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
}
