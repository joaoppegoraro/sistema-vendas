package com.salessystem.dto.report;

public class PaymentTotalLineDTO {

    private String paymentMethodLabel;
    private String formattedTotal;
    private String checkHint;

    public String getPaymentMethodLabel() {
        return paymentMethodLabel;
    }

    public void setPaymentMethodLabel(String paymentMethodLabel) {
        this.paymentMethodLabel = paymentMethodLabel;
    }

    public String getFormattedTotal() {
        return formattedTotal;
    }

    public void setFormattedTotal(String formattedTotal) {
        this.formattedTotal = formattedTotal;
    }

    public String getCheckHint() {
        return checkHint;
    }

    public void setCheckHint(String checkHint) {
        this.checkHint = checkHint;
    }
}
