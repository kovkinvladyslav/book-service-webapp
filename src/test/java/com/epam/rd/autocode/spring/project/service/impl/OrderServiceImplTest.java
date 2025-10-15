package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import com.epam.rd.autocode.spring.project.repository.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repository.OrderRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private GenericMapper<Order, OrderDTO> orderMapper;
    private BookService bookService;
    private ClientRepository clientRepository;
    private PaymentServiceImpl paymentService;
    private EmployeeRepository employeeRepository;
    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderMapper = mock(GenericMapper.class);
        bookService = mock(BookService.class);
        clientRepository = mock(ClientRepository.class);
        paymentService = mock(PaymentServiceImpl.class);
        employeeRepository = mock(EmployeeRepository.class);
        service = new OrderServiceImpl(orderRepository, orderMapper, bookService, clientRepository, paymentService, employeeRepository);
    }

    @Test
    void getOrdersByClient_mapsList() {
        when(orderRepository.findByClientEmail("c")).thenReturn(List.of(new Order()));
        when(orderMapper.toDtoList(anyList())).thenReturn(List.of(new OrderDTO()));
        assertThat(service.getOrdersByClient("c")).hasSize(1);
    }

    @Test
    void getOrdersByEmployee_mapsList() {
        when(orderRepository.findByEmployeeEmail("e")).thenReturn(List.of(new Order()));
        when(orderMapper.toDtoList(anyList())).thenReturn(List.of(new OrderDTO()));
        assertThat(service.getOrdersByEmployee("e")).hasSize(1);
    }

    @Test
    void addOrder_mapsAndSaves() {
        OrderDTO dto = new OrderDTO();
        Order ent = new Order();
        Order saved = new Order();
        OrderDTO mapped = new OrderDTO();
        when(orderMapper.toEntity(dto)).thenReturn(ent);
        when(orderRepository.save(ent)).thenReturn(saved);
        when(orderMapper.toDto(saved)).thenReturn(mapped);
        assertThat(service.addOrder(dto)).isSameAs(mapped);
    }

    @Test
    void addBookToOrder_whenDraftExists_andItemExists_incrementsQuantity() {
        Order draft = new Order();
        draft.setOrderStatus(OrderStatus.DRAFT);
        BookItem it = new BookItem();
        Book book = new Book(); book.setName("B"); book.setPrice(new BigDecimal("3"));
        it.setBook(book); it.setQuantity(1);
        draft.setBookItems(new ArrayList<>(List.of(it)));
        when(orderRepository.findByClientEmailAndOrderStatus("c", OrderStatus.DRAFT)).thenReturn(Optional.of(draft));

        service.addBookToOrder("B", "c");

        assertThat(draft.getBookItems().get(0).getQuantity()).isEqualTo(2);
        verify(orderRepository).save(draft);
    }

    @Test
    void addBookToOrder_whenDraftHasNullItems_initializesList_addsItem_andSaves() {
        Order draft = new Order();
        draft.setOrderStatus(OrderStatus.DRAFT);
        draft.setBookItems(null);

        when(orderRepository.findByClientEmailAndOrderStatus("c", OrderStatus.DRAFT))
                .thenReturn(java.util.Optional.of(draft));

        Book book = new Book();
        book.setName("B");
        book.setPrice(new BigDecimal("7"));
        when(bookService.getEntityByName("B")).thenReturn(book);

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addBookToOrder("B", "c");

        assertThat(draft.getBookItems()).isNotNull();
        assertThat(draft.getBookItems()).hasSize(1);
        BookItem bi = draft.getBookItems().get(0);
        assertThat(bi.getBook()).isSameAs(book);
        assertThat(bi.getQuantity()).isEqualTo(1);
        verify(orderRepository, atLeastOnce()).save(draft);
    }


    @Test
    void addBookToOrder_whenDraftNotExists_createsAndAddsNewItem() {
        when(orderRepository.findByClientEmailAndOrderStatus("c", OrderStatus.DRAFT)).thenReturn(Optional.empty());
        Client client = new Client(); client.setEmail("c");
        when(clientRepository.findByEmail("c")).thenReturn(Optional.of(client));
        Book ent = new Book(); ent.setName("X"); ent.setPrice(new BigDecimal("5"));
        when(bookService.getEntityByName("X")).thenReturn(ent);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        service.addBookToOrder("X", "c");

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void removeBookFromOrder_decrementQuantity() {
        Order draft = new Order();
        draft.setOrderStatus(OrderStatus.DRAFT);
        BookItem it = new BookItem();
        Book book = new Book(); book.setName("B");
        it.setBook(book); it.setQuantity(2);
        draft.setBookItems(new ArrayList<>(List.of(it)));
        when(orderRepository.findByClientEmailAndOrderStatus("c", OrderStatus.DRAFT)).thenReturn(Optional.of(draft));

        service.removeBookFromOrder("B", "c");

        assertThat(draft.getBookItems().get(0).getQuantity()).isEqualTo(1);
        verify(orderRepository).save(draft);
    }

    @Test
    void removeBookFromOrder_removeItemWhenQuantityBecomesZero() {
        Order draft = new Order();
        draft.setOrderStatus(OrderStatus.DRAFT);
        BookItem it = new BookItem();
        Book book = new Book(); book.setName("B");
        it.setBook(book); it.setQuantity(1);
        draft.setBookItems(new ArrayList<>(List.of(it)));
        when(orderRepository.findByClientEmailAndOrderStatus("c", OrderStatus.DRAFT)).thenReturn(Optional.of(draft));

        service.removeBookFromOrder("B", "c");

        assertThat(draft.getBookItems()).isEmpty();
        verify(orderRepository).save(draft);
    }

    @Test
    void removeBookFromOrder_noDraft_throws() {
        when(orderRepository.findByClientEmailAndOrderStatus("c", OrderStatus.DRAFT)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.removeBookFromOrder("B", "c")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void placeOrder_noDraft_throws() {
        when(orderRepository.findByClientEmailAndOrderStatus("c", OrderStatus.DRAFT)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.placeOrder("c")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void placeOrder_insufficientBalance_throws() {
        Order draft = new Order();
        draft.setOrderStatus(OrderStatus.DRAFT);
        BookItem it = new BookItem();
        Book b = new Book(); b.setPrice(new BigDecimal("10"));
        it.setBook(b); it.setQuantity(1);
        draft.setBookItems(List.of(it));
        when(orderRepository.findByClientEmailAndOrderStatus("c", OrderStatus.DRAFT)).thenReturn(Optional.of(draft));
        when(paymentService.processPayment("c", new BigDecimal("10"))).thenReturn(false);

        assertThatThrownBy(() -> service.placeOrder("c")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void placeOrder_success_setsTotalsStatusAndDate() {
        Order draft = new Order();
        draft.setOrderStatus(OrderStatus.DRAFT);
        BookItem it = new BookItem();
        Book b = new Book(); b.setPrice(new BigDecimal("4"));
        it.setBook(b); it.setQuantity(2);
        draft.setBookItems(List.of(it));
        when(orderRepository.findByClientEmailAndOrderStatus("c", OrderStatus.DRAFT)).thenReturn(Optional.of(draft));
        when(paymentService.processPayment("c", new BigDecimal("8"))).thenReturn(true);

        service.placeOrder("c");

        assertThat(draft.getPrice()).isEqualByComparingTo(new BigDecimal("8"));
        assertThat(draft.getOrderStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(draft.getOrderDate()).isNotNull();
        verify(orderRepository).save(draft);
    }

    @Test
    void assignOrderToEmployee_notFound_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.assignOrderToEmployee(1L, "e")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void assignOrderToEmployee_wrongStatus_throws() {
        Order o = new Order(); o.setOrderStatus(OrderStatus.DRAFT);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));
        assertThatThrownBy(() -> service.assignOrderToEmployee(1L, "e")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void assignOrderToEmployee_alreadyAssigned_throws() {
        Order o = new Order(); o.setOrderStatus(OrderStatus.PLACED); o.setEmployee(new Employee());
        when(orderRepository.findById(2L)).thenReturn(Optional.of(o));
        assertThatThrownBy(() -> service.assignOrderToEmployee(2L, "e")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void assignOrderToEmployee_success_setsEmployeeAndProcessing() {
        Order o = new Order(); o.setOrderStatus(OrderStatus.PLACED);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(o));
        Employee emp = new Employee(); emp.setEmail("e");
        when(employeeRepository.findByEmail("e")).thenReturn(Optional.of(emp));

        service.assignOrderToEmployee(3L, "e");

        assertThat(o.getEmployee()).isSameAs(emp);
        assertThat(o.getOrderStatus()).isEqualTo(OrderStatus.PROCESSING);
        verify(orderRepository).save(o);
    }

    @Test
    void markOrderAsCompleted_setsCompleted() {
        Order o = new Order(); o.setOrderStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(o));

        service.markOrderAsCompleted(5L);

        assertThat(o.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(orderRepository).save(o);
    }

    @Test
    void markOrderAsCompleted_notFound_throws() {
        when(orderRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.markOrderAsCompleted(7L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOrdersByStatus_mapsList() {
        when(orderRepository.findByOrderStatus(OrderStatus.PLACED)).thenReturn(List.of(new Order()));
        when(orderMapper.toDtoList(anyList())).thenReturn(List.of(new OrderDTO()));
        assertThat(service.getOrdersByStatus(OrderStatus.PLACED)).hasSize(1);
    }

    @Test
    void cancelOrder_notFound_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cancelOrder("c", 1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void cancelOrder_otherUser_throws() {
        Order o = new Order(); Client cl = new Client(); cl.setEmail("x"); o.setClient(cl);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));
        assertThatThrownBy(() -> service.cancelOrder("c", 1L)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelOrder_wrongStatus_throws() {
        Order o = new Order(); Client cl = new Client(); cl.setEmail("c"); o.setClient(cl);
        o.setOrderStatus(OrderStatus.DRAFT);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));
        assertThatThrownBy(() -> service.cancelOrder("c", 1L)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelOrder_refundAndCancel() {
        Order o = new Order();
        Client cl = new Client(); cl.setEmail("c"); o.setClient(cl);
        o.setOrderStatus(OrderStatus.PLACED);
        o.setPrice(new BigDecimal("12"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));

        service.cancelOrder("c", 1L);

        verify(paymentService).deposit("c", new BigDecimal("12"));
        assertThat(o.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(o);
    }

    @Test
    void getAllOrders_mapsList() {
        when(orderRepository.findAll()).thenReturn(List.of(new Order()));
        when(orderMapper.toDtoList(anyList())).thenReturn(List.of(new OrderDTO()));
        assertThat(service.getAllOrders()).hasSize(1);
    }
}
