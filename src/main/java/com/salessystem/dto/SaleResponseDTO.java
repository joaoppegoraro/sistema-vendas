package com.salessystem.dto;

import com.salessystem.entity.SaleStatus;

import java.time.LocalDateTime;
import java.util.List;

public class SaleResponseDTO {

    private Long id;
    private Long clientId;
    private String clientName;
    private String clientCpf;
    private LocalDateTime saleDate;
    private String formattedSaleDate;
    private String formattedDiscount;
    private String formattedSurcharge;
    private String paymentMethodLabel;
    private SaleStatus status;
    private String statusLabel;
    private String formattedTotal;
    private List<SaleItemResponseDTO> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientCpf() {
        return clientCpf;
    }

    public void setClientCpf(String clientCpf) {
        this.clientCpf = clientCpf;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public String getFormattedSaleDate() {
        return formattedSaleDate;
    }

    public void setFormattedSaleDate(String formattedSaleDate) {
        this.formattedSaleDate = formattedSaleDate;
    }

    public String getFormattedDiscount() {
        return formattedDiscount;
    }

    public void setFormattedDiscount(String formattedDiscount) {
        this.formattedDiscount = formattedDiscount;
    }

    public String getFormattedSurcharge() {
        return formattedSurcharge;
    }

    public void setFormattedSurcharge(String formattedSurcharge) {
        this.formattedSurcharge = formattedSurcharge;
    }

    public String getPaymentMethodLabel() {
        return paymentMethodLabel;
    }

    public void setPaymentMethodLabel(String paymentMethodLabel) {
        this.paymentMethodLabel = paymentMethodLabel;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getFormattedTotal() {
        return formattedTotal;
    }

    public void setFormattedTotal(String formattedTotal) {
        this.formattedTotal = formattedTotal;
    }

    public List<SaleItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<SaleItemResponseDTO> items) {
        this.items = items;
    }
}
