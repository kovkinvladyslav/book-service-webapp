package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    private OrderService orderService;
    private EmployeeService employeeService;
    private EmployeeController controller;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        employeeService = mock(EmployeeService.class);
        controller = new EmployeeController(orderService, employeeService);
    }

    private TestingAuthenticationToken auth(String email) {
        return new TestingAuthenticationToken(
                email, "x", List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
    }

    @Test
    void viewOrders_populatesEmployee_myOrders_newOrders_andReturnsView() {
        when(employeeService.getEmployeeByEmail("e@ex.com")).thenReturn(new com.epam.rd.autocode.spring.project.dto.EmployeeDTO());
        when(orderService.getOrdersByEmployee("e@ex.com")).thenReturn(List.of());
        when(orderService.getOrdersByStatus(OrderStatus.PLACED)).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = controller.viewOrders(auth("e@ex.com"), model);

        assertThat(view).isEqualTo("employee/manage-orders");
        assertThat(model.getAttribute("employee")).isNotNull();
        assertThat(model.getAttribute("myOrders")).isInstanceOf(List.class);
        assertThat(model.getAttribute("newOrders")).isInstanceOf(List.class);
    }

    @Test
    void acceptOrder_assignsAndRedirects() {
        String view = controller.acceptOrder(42L, auth("e@ex.com"));

        assertThat(view).isEqualTo("redirect:/employee/orders");
        verify(orderService).assignOrderToEmployee(42L, "e@ex.com");
    }

    @Test
    void completeOrder_marksCompleted_andRedirects() {
        String view = controller.completeOrder(15L);

        assertThat(view).isEqualTo("redirect:/employee/orders");
        verify(orderService).markOrderAsCompleted(15L);
    }

    @Test
    void profile_addsEmployee_andReturnsView() {
        var employee = new com.epam.rd.autocode.spring.project.dto.EmployeeDTO();
        when(employeeService.getEmployeeByEmail("e@ex.com")).thenReturn(employee);

        Model model = new ExtendedModelMap();
        String view = controller.profile(model, auth("e@ex.com"));

        assertThat(view).isEqualTo("employee/profile");
        assertThat(model.getAttribute("employee")).isEqualTo(employee);
    }
}
