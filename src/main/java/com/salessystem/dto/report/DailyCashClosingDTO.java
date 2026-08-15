package com.salessystem.dto.report;

import java.time.LocalDate;
import java.util.List;

public class DailyCashClosingDTO {

    private LocalDate date;
    private String formattedDate;
    private String formattedTotal;
    private List<PaymentTotalLineDTO> lines;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public String getFormattedTotal() {
        return formattedTotal;
    }

    public void setFormattedTotal(String formattedTotal) {
        this.formattedTotal = formattedTotal;
    }

    public List<PaymentTotalLineDTO> getLines() {
        return lines;
    }

    public void setLines(List<PaymentTotalLineDTO> lines) {
        this.lines = lines;
    }
}
