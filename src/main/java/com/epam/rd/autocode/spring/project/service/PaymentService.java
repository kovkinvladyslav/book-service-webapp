package com.epam.rd.autocode.spring.project.service;

import java.math.BigDecimal;

public interface PaymentService {

    void withdraw(String clientEmail, BigDecimal amount);
    void deposit(String clientEmail, BigDecimal amount);
    boolean processPayment(String clientEmail, BigDecimal amount);
}
