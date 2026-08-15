package com.salessystem.dto;

import java.util.List;

public class DashboardSummaryDTO {

    private String formattedMonthlyRevenue;
    private String formattedMonthlyProfit;
    private String formattedStockValue;
    private List<TopProductDTO> topProducts;

    public String getFormattedMonthlyRevenue() {
        return formattedMonthlyRevenue;
    }

    public void setFormattedMonthlyRevenue(String formattedMonthlyRevenue) {
        this.formattedMonthlyRevenue = formattedMonthlyRevenue;
    }

    public String getFormattedMonthlyProfit() {
        return formattedMonthlyProfit;
    }

    public void setFormattedMonthlyProfit(String formattedMonthlyProfit) {
        this.formattedMonthlyProfit = formattedMonthlyProfit;
    }

    public String getFormattedStockValue() {
        return formattedStockValue;
    }

    public void setFormattedStockValue(String formattedStockValue) {
        this.formattedStockValue = formattedStockValue;
    }

    public List<TopProductDTO> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopProductDTO> topProducts) {
        this.topProducts = topProducts;
    }
}
