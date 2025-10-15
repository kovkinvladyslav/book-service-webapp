package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    private OrderService orderService;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        controller = new OrderController(orderService);
    }

    private TestingAuthenticationToken auth(String email) {
        return new TestingAuthenticationToken(
                email, "x", List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
    }

    @Test
    void addToCart_callsService_andRedirectsToCart() {
        String view = controller.addToCart("BookName", auth("c@ex.com"));
        assertThat(view).isEqualTo("redirect:/orders/cart");
        verify(orderService).addBookToOrder("BookName", "c@ex.com");
    }

    @Test
    void removeFromCart_callsService_andRedirectsToCart() {
        String view = controller.removeFromCart("BookName", auth("c@ex.com"));
        assertThat(view).isEqualTo("redirect:/orders/cart");
        verify(orderService).removeBookFromOrder("BookName", "c@ex.com");
    }

    @Test
    void viewCart_withDraftOrder_setsOrderInModel() {
        OrderDTO draft = new OrderDTO();
        draft.setOrderStatus(OrderStatus.DRAFT);
        List<OrderDTO> list = new ArrayList<>();
        list.add(draft);
        when(orderService.getOrdersByClient("c@ex.com")).thenReturn(list);

        Model model = new ExtendedModelMap();
        String view = controller.viewCart(auth("c@ex.com"), model);

        assertThat(view).isEqualTo("client/cart");
        assertThat(model.getAttribute("order")).isEqualTo(draft);
    }

    @Test
    void viewCart_withoutDraft_setsNullOrder() {
        OrderDTO placed = new OrderDTO();
        placed.setOrderStatus(OrderStatus.PLACED);
        when(orderService.getOrdersByClient("c@ex.com")).thenReturn(List.of(placed));

        Model model = new ExtendedModelMap();
        String view = controller.viewCart(auth("c@ex.com"), model);

        assertThat(view).isEqualTo("client/cart");
        assertThat(model.getAttribute("order")).isNull();
    }

    @Test
    void checkout_placesOrder_andRedirectsToOrders() {
        String view = controller.placeOrder(auth("c@ex.com"));
        assertThat(view).isEqualTo("redirect:/orders");
        verify(orderService).placeOrder("c@ex.com");
    }

    @Test
    void listClientOrders_filtersOutDraft_andReturnsView() {
        OrderDTO draft = new OrderDTO(); draft.setOrderStatus(OrderStatus.DRAFT);
        OrderDTO placed = new OrderDTO(); placed.setOrderStatus(OrderStatus.PLACED);
        when(orderService.getOrdersByClient("c@ex.com")).thenReturn(List.of(draft, placed));

        Model model = new ExtendedModelMap();
        String view = controller.listClientOrders(auth("c@ex.com"), model);

        assertThat(view).isEqualTo("client/orders");
        @SuppressWarnings("unchecked")
        List<OrderDTO> orders = (List<OrderDTO>) model.getAttribute("orders");
        assertThat(orders).containsExactly(placed);
    }

    @Test
    void cancelOrder_callsService_andRedirects() {
        String view = controller.cancelOrder(123L, auth("c@ex.com"));
        assertThat(view).isEqualTo("redirect:/orders");
        verify(orderService).cancelOrder("c@ex.com", 123L);
    }
}
