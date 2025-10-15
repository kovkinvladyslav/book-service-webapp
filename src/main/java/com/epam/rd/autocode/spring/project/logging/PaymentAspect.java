package com.epam.rd.autocode.spring.project.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Aspect
@Component
public class PaymentAspect {

    @After("execution(* com.epam.rd.autocode.spring.project.service.PaymentService.deposit(..)) && args(clientEmail, amount)")
    public void onDeposit(String clientEmail, BigDecimal amount) {
        log.info("[PAYMENT] deposit client={} amount={}", LogSanitizer.maskEmail(clientEmail), amount);
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.PaymentService.withdraw(..)) && args(clientEmail, amount)")
    public void onWithdraw(String clientEmail, BigDecimal amount) {
        log.info("[PAYMENT] withdraw client={} amount={}", LogSanitizer.maskEmail(clientEmail), amount);
    }

    @AfterReturning(pointcut = "execution(* com.epam.rd.autocode.spring.project.service.PaymentService.processPayment(..)) && args(clientEmail, amount)", returning = "ok")
    public void onProcess(String clientEmail, BigDecimal amount, boolean ok) {
        log.info("[PAYMENT] process client={} amount={} ok={}", LogSanitizer.maskEmail(clientEmail), amount, ok);
    }

    @AfterThrowing(pointcut = "execution(* com.epam.rd.autocode.spring.project.service.PaymentService.*(..))", throwing = "ex")
    public void onPaymentError(Throwable ex) {
        log.error("[PAYMENT] error {}", ex.toString());
    }
}
