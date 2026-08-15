package com.salessystem.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ClientPurchaseProjection {

    Long getClientId();

    String getClientName();

    String getClientPhone();

    BigDecimal getTotalSpent();

    Long getPurchaseCount();

    LocalDateTime getLastPurchaseDate();
}
