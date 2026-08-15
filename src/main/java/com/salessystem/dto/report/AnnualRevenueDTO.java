package com.salessystem.dto.report;

import java.util.List;

public class AnnualRevenueDTO {

    private int year;
    private List<MonthlyRevenueDTO> months;
    private String formattedAnnualTotal;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<MonthlyRevenueDTO> getMonths() {
        return months;
    }

    public void setMonths(List<MonthlyRevenueDTO> months) {
        this.months = months;
    }

    public String getFormattedAnnualTotal() {
        return formattedAnnualTotal;
    }

    public void setFormattedAnnualTotal(String formattedAnnualTotal) {
        this.formattedAnnualTotal = formattedAnnualTotal;
    }
}
