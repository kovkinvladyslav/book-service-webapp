package com.epam.rd.autocode.spring.project.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import com.epam.rd.autocode.spring.project.repository.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.repository.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{
    private final OrderRepository orderRepository;
    private final GenericMapper<Order, OrderDTO> orderMapper;
    private final BookService bookService;
    private final ClientRepository clientRepository;
    private final PaymentServiceImpl paymentService;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        return orderMapper.toDtoList(orderRepository.findByClientEmail(clientEmail));
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        return orderMapper.toDtoList(orderRepository.findByEmployeeEmail(employeeEmail));
    }

    @Override
    public OrderDTO addOrder(OrderDTO orderDTO) {
        Order order = orderMapper.toEntity(orderDTO);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public void addBookToOrder(String bookName, String clientEmail) {
        Order draftOrder = orderRepository
                .findByClientEmailAndOrderStatus(clientEmail, OrderStatus.DRAFT)
                .orElseGet(() -> {
                    Client client = clientRepository.findByEmail(clientEmail)
                            .orElseThrow(() -> new NotFoundException("Client not found: " + clientEmail));
                    Order newOrder = new Order();
                    newOrder.setClient(client);
                    newOrder.setOrderStatus(OrderStatus.DRAFT);
                    newOrder.setOrderDate(LocalDateTime.now());
                    newOrder.setBookItems(new ArrayList<>());
                    newOrder.setPrice(BigDecimal.ZERO);
                    return orderRepository.save(newOrder);
                });

        List<BookItem> items = draftOrder.getBookItems();
        if (items == null) {
            items = new ArrayList<>();
            draftOrder.setBookItems(items);
        }

        for (BookItem item : items) {
            if (item.getBook().getName().equals(bookName)) {
                item.setQuantity(item.getQuantity() + 1);
                recalculateOrderPrice(draftOrder);
                orderRepository.save(draftOrder);
                return;
            }
        }

        Book book = bookService.getEntityByName(bookName);
        BookItem newItem = new BookItem();
        newItem.setBook(book);
        newItem.setOrder(draftOrder);
        newItem.setQuantity(1);
        items.add(newItem);

        recalculateOrderPrice(draftOrder);
        orderRepository.save(draftOrder);
    }


    @Override
    public void removeBookFromOrder(String bookName, String clientEmail) {
        Order draftOrder = orderRepository
                .findByClientEmailAndOrderStatus(clientEmail, OrderStatus.DRAFT)
                .orElseThrow(() -> new NotFoundException("No draft order found for " + clientEmail));

        List<BookItem> items = draftOrder.getBookItems();
        if (items == null || items.isEmpty()) return;

        for (Iterator<BookItem> it = items.iterator(); it.hasNext();) {
            BookItem item = it.next();
            if (item.getBook().getName().equals(bookName)) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                } else {
                    it.remove();
                }
                orderRepository.save(draftOrder);
                return;
            }
        }
    }

    @Override
    public void placeOrder(String clientEmail) {
        Order draft = orderRepository.findByClientEmailAndOrderStatus(clientEmail, OrderStatus.DRAFT)
                .orElseThrow(() -> new NotFoundException("No draft order for " + clientEmail));

        BigDecimal total = draft.getBookItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!paymentService.processPayment(clientEmail, total)) {
            throw new IllegalStateException("Insufficient balance. Please top up your account.");
        }

        draft.setPrice(total);
        draft.setOrderStatus(OrderStatus.PLACED);
        draft.setOrderDate(LocalDateTime.now());

        orderRepository.save(draft);
    }

    private void recalculateOrderPrice(Order order) {
        BigDecimal total = BigDecimal.ZERO;

        if (order.getBookItems() != null) {
            for (BookItem item : order.getBookItems()) {
                if (item.getBook() != null && item.getBook().getPrice() != null) {
                    total = total.add(item.getBook().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        }

        order.setPrice(total);
    }

    @Override
    @Transactional
    public void assignOrderToEmployee(Long orderId, String employeeEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (order.getOrderStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException("Order must be PLACED before accepting");
        }
        if (order.getEmployee() != null) {
            throw new IllegalStateException("Order already assigned");
        }

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeEmail));

        order.setEmployee(employee);
        order.setOrderStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markOrderAsCompleted(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        order.setOrderStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        return orderMapper.toDtoList(orderRepository.findByOrderStatus(status));
    }

    @Override
    @Transactional
    public void cancelOrder(String clientEmail, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!order.getClient().getEmail().equals(clientEmail)) {
            throw new IllegalStateException("You can only cancel your own orders.");
        }

        if (order.getOrderStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException("Only PLACED orders can be cancelled.");
        }

        BigDecimal refundAmount = order.getPrice();
        if (refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            paymentService.deposit(clientEmail, refundAmount);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

}
