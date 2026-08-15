package com.salessystem.dto.report;

import java.util.List;

public class ClientAnalyticsDTO {

    private int inactiveDays;
    private List<ClientPurchaseSummaryDTO> topClients;
    private List<ClientPurchaseSummaryDTO> churnedClients;
    private List<ClientPurchaseSummaryDTO> oneTimeClients;

    public int getInactiveDays() {
        return inactiveDays;
    }

    public void setInactiveDays(int inactiveDays) {
        this.inactiveDays = inactiveDays;
    }

    public List<ClientPurchaseSummaryDTO> getTopClients() {
        return topClients;
    }

    public void setTopClients(List<ClientPurchaseSummaryDTO> topClients) {
        this.topClients = topClients;
    }

    public List<ClientPurchaseSummaryDTO> getChurnedClients() {
        return churnedClients;
    }

    public void setChurnedClients(List<ClientPurchaseSummaryDTO> churnedClients) {
        this.churnedClients = churnedClients;
    }

    public List<ClientPurchaseSummaryDTO> getOneTimeClients() {
        return oneTimeClients;
    }

    public void setOneTimeClients(List<ClientPurchaseSummaryDTO> oneTimeClients) {
        this.oneTimeClients = oneTimeClients;
    }
}
