package com.epam.rd.autocode.spring.project.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class OrderLifecycleAspect {

    @After("execution(* com.epam.rd.autocode.spring.project.service.OrderService.addBookToOrder(..)) && args(bookName, clientEmail)")
    public void onCartAdd(String bookName, String clientEmail) {
        log.info("[CART] add book={} client={}", LogSanitizer.mask(bookName), LogSanitizer.maskEmail(clientEmail));
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.OrderService.removeBookFromOrder(..)) && args(bookName, clientEmail)")
    public void onCartRemove(String bookName, String clientEmail) {
        log.info("[CART] remove book={} client={}", LogSanitizer.mask(bookName), LogSanitizer.maskEmail(clientEmail));
    }

    @AfterReturning("execution(* com.epam.rd.autocode.spring.project.service.OrderService.placeOrder(..)) && args(clientEmail)")
    public void onPlaced(String clientEmail) {
        log.info("[ORDER] placed by client={}", LogSanitizer.maskEmail(clientEmail));
    }

    @AfterThrowing(pointcut = "execution(* com.epam.rd.autocode.spring.project.service.OrderService.placeOrder(..)) && args(clientEmail)", throwing = "ex")
    public void onPlaceFailed(String clientEmail, Throwable ex) {
        log.warn("[ORDER] place failed client={} reason={}", LogSanitizer.maskEmail(clientEmail), ex.toString());
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.OrderService.assignOrderToEmployee(..)) && args(orderId, employeeEmail)")
    public void onAssigned(Long orderId, String employeeEmail) {
        log.info("[ORDER] assigned id={} to employee={}", orderId, LogSanitizer.maskEmail(employeeEmail));
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.OrderService.markOrderAsCompleted(..)) && args(orderId)")
    public void onCompleted(Long orderId) {
        log.info("[ORDER] completed id={}", orderId);
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.OrderService.cancelOrder(..)) && args(clientEmail, orderId)")
    public void onCancelled(String clientEmail, Long orderId) {
        log.warn("[ORDER] cancelled id={} by client={}", orderId, LogSanitizer.maskEmail(clientEmail));
    }
}
