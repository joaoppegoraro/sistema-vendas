package com.salessystem.dto.report;

import java.math.BigDecimal;

public class ClientPurchaseSummaryDTO {

    private Long clientId;
    private String clientName;
    private String whatsAppUrl;
    private BigDecimal totalSpent;
    private String formattedTotalSpent;
    private long purchaseCount;
    private String formattedLastPurchaseDate;
    private long daysSinceLastPurchase;

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
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

    public String getWhatsAppUrl() {
        return whatsAppUrl;
    }

    public void setWhatsAppUrl(String whatsAppUrl) {
        this.whatsAppUrl = whatsAppUrl;
    }

    public String getFormattedTotalSpent() {
        return formattedTotalSpent;
    }

    public void setFormattedTotalSpent(String formattedTotalSpent) {
        this.formattedTotalSpent = formattedTotalSpent;
    }

    public long getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(long purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    public String getFormattedLastPurchaseDate() {
        return formattedLastPurchaseDate;
    }

    public void setFormattedLastPurchaseDate(String formattedLastPurchaseDate) {
        this.formattedLastPurchaseDate = formattedLastPurchaseDate;
    }

    public long getDaysSinceLastPurchase() {
        return daysSinceLastPurchase;
    }

    public void setDaysSinceLastPurchase(long daysSinceLastPurchase) {
        this.daysSinceLastPurchase = daysSinceLastPurchase;
    }
}
