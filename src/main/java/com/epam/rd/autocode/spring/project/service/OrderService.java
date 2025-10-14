package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;

import java.util.*;

public interface OrderService {

    List<OrderDTO> getOrdersByClient(String clientEmail);

    List<OrderDTO> getOrdersByEmployee(String employeeEmail);

    OrderDTO addOrder(OrderDTO order);

    void addBookToOrder(String bookName, String clientEmail);

    void removeBookFromOrder(String bookName, String clientEmail);

    void placeOrder(String clientEmail);

    void assignOrderToEmployee(Long id, String name);

    void markOrderAsCompleted(Long id);

    List<OrderDTO> getOrdersByStatus(OrderStatus status);

    void cancelOrder(String clientEmail, Long orderId);
}
