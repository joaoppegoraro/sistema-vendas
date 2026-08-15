package com.salessystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class ProductVariantRequestDTO {

    @Size(max = 20, message = "O tamanho deve ter no máximo 20 caracteres")
    private String size;

    @Size(max = 40, message = "A cor deve ter no máximo 40 caracteres")
    private String color;

    @Min(value = 0, message = "O estoque inicial não pode ser negativo")
    private int initialStock;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getInitialStock() {
        return initialStock;
    }

    public void setInitialStock(int initialStock) {
        this.initialStock = initialStock;
    }
}
