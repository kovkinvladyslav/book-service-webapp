package com.epam.rd.autocode.spring.project.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.repository.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{
    final private OrderRepository orderRepository;
    final private GenericMapper<Order, OrderDTO> orderMapper;
    final private ClientService clientService;
    final private GenericMapper<Client, ClientDTO> clientMapper;
    final private BookService bookService;
    final private GenericMapper<Book, BookDTO> bookMapper;
    final private ClientRepository clientRepository;
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
                .orElseThrow(() -> new NotFoundException("No draft order for" + clientEmail));
        draft.setOrderStatus(OrderStatus.PLACED);
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

}
