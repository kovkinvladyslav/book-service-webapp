package com.epam.rd.autocode.spring.project.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class BizTimingAspect {

    @Around("execution(* com.epam.rd.autocode.spring.project.service.OrderService.placeOrder(..)) || " +
            "execution(* com.epam.rd.autocode.spring.project.service.OrderService.assignOrderToEmployee(..)) || " +
            "execution(* com.epam.rd.autocode.spring.project.service.PaymentService.*(..))")
    public Object measure(ProceedingJoinPoint pjp) throws Throwable {
        long t0 = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long took = System.currentTimeMillis() - t0;
            String m = pjp.getSignature().toShortString();
            log.info("[TIMING] {} took={}ms", m, took);
        }
    }
}
