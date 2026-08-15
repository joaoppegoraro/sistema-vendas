package com.salessystem.entity;

public enum SaleStatus {

    COMPLETED("Concluída"),
    CANCELLED("Cancelada");

    private final String label;

    SaleStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
