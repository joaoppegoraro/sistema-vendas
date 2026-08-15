package com.salessystem.dto.report;

public class ProfitabilityReportDTO {

    private String monthLabel;
    private String formattedRevenue;
    private String formattedCost;
    private String formattedProfit;

    public String getMonthLabel() {
        return monthLabel;
    }

    public void setMonthLabel(String monthLabel) {
        this.monthLabel = monthLabel;
    }

    public String getFormattedRevenue() {
        return formattedRevenue;
    }

    public void setFormattedRevenue(String formattedRevenue) {
        this.formattedRevenue = formattedRevenue;
    }

    public String getFormattedCost() {
        return formattedCost;
    }

    public void setFormattedCost(String formattedCost) {
        this.formattedCost = formattedCost;
    }

    public String getFormattedProfit() {
        return formattedProfit;
    }

    public void setFormattedProfit(String formattedProfit) {
        this.formattedProfit = formattedProfit;
    }
}
