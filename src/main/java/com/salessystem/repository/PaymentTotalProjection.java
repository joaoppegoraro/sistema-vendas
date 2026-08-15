package com.salessystem.repository;

import com.salessystem.entity.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentTotalProjection {

    PaymentMethod getPaymentMethod();

    BigDecimal getTotal();
}
