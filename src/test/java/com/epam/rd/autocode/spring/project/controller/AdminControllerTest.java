package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    private EmployeeService employeeService;
    private OrderService orderService;
    private PasswordEncoder passwordEncoder;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        employeeService = mock(EmployeeService.class);
        orderService = mock(OrderService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        controller = new AdminController(employeeService, orderService, passwordEncoder);
        ReflectionTestUtils.setField(controller, "adminName", "Test Admin");
    }

    @Test
    void dashboard_populatesCountsAndReturnsView() {
        when(employeeService.getAllEmployees()).thenReturn(List.of(new EmployeeDTO(), new EmployeeDTO()));
        OrderDTO o1 = new OrderDTO(); o1.setOrderStatus(OrderStatus.PLACED);
        OrderDTO o2 = new OrderDTO(); o2.setOrderStatus(OrderStatus.PROCESSING);
        OrderDTO o3 = new OrderDTO(); o3.setOrderStatus(OrderStatus.COMPLETED);
        OrderDTO o4 = new OrderDTO(); o4.setOrderStatus(OrderStatus.PLACED);
        when(orderService.getAllOrders()).thenReturn(List.of(o1, o2, o3, o4));

        Model model = new ExtendedModelMap();
        String view = controller.dashboard(model, /* auth */ null);

        assertThat(view).isEqualTo("admin/dashboard");
        assertThat(model.getAttribute("adminName")).isEqualTo("Test Admin");
        assertThat(model.getAttribute("employeeCount")).isEqualTo(2);
        assertThat(model.getAttribute("totalOrders")).isEqualTo(4);
        assertThat(model.getAttribute("placedOrders")).isEqualTo(2L);
        assertThat(model.getAttribute("processingOrders")).isEqualTo(1L);
        assertThat(model.getAttribute("completedOrders")).isEqualTo(1L);
    }

    @Test
    void listEmployees_addsToModel_andReturnsView() {
        when(employeeService.getAllEmployees()).thenReturn(List.of(new EmployeeDTO()));

        Model model = new ExtendedModelMap();
        String view = controller.listEmployees(model);

        assertThat(view).isEqualTo("admin/employees");
        assertThat(model.getAttribute("employees")).isInstanceOf(List.class);
    }

    @Test
    void updateEmployee_withNullPassword_usesExistingPassword_redirects() {
        EmployeeDTO incoming = new EmployeeDTO();
        incoming.setPassword(null);

        EmployeeDTO existing = new EmployeeDTO();
        existing.setPassword("EXISTING_HASH");

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        when(employeeService.getEmployeeByEmail("e@x")).thenReturn(existing);

        String view = controller.updateEmployee("e@x", incoming, br);

        assertThat(view).isEqualTo("redirect:/admin/employees");
        assertThat(incoming.getPassword()).isEqualTo("EXISTING_HASH");
        verify(employeeService).updateEmployeeByEmail("e@x", incoming);
        verifyNoInteractions(passwordEncoder);
    }


    @Test
    void showAddEmployeeForm_initializesDto_andReturnsView() {
        Model model = new ExtendedModelMap();

        String view = controller.showAddEmployeeForm(model);

        assertThat(view).isEqualTo("admin/employee-add");
        assertThat(model.getAttribute("employeeDTO")).isInstanceOf(EmployeeDTO.class);
    }

    @Test
    void addEmployee_whenValidationErrors_returnsForm() {
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);

        String view = controller.addEmployee(new EmployeeDTO(), br);

        assertThat(view).isEqualTo("admin/employee-add");
        verifyNoInteractions(employeeService);
    }

    @Test
    void addEmployee_success_encodesPassword_callsService_redirects() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setPassword("raw");
        when(passwordEncoder.encode("raw")).thenReturn("ENCODED");
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        String view = controller.addEmployee(dto, br);

        assertThat(view).isEqualTo("redirect:/admin/employees");
        assertThat(dto.getPassword()).isEqualTo("ENCODED");
        verify(employeeService).addEmployee(dto);
    }

    @Test
    void showEditEmployeeForm_loadsDto_andReturnsView() {
        EmployeeDTO existing = new EmployeeDTO();
        existing.setEmail("a@b.c");
        when(employeeService.getEmployeeByEmail("a@b.c")).thenReturn(existing);
        Model model = new ExtendedModelMap();

        String view = controller.showEditEmployeeForm("a@b.c", model);

        assertThat(view).isEqualTo("admin/employee-edit");
        assertThat(model.getAttribute("employeeDTO")).isEqualTo(existing);
    }

    @Test
    void updateEmployee_whenValidationErrors_returnsForm() {
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);

        String view = controller.updateEmployee("a@b.c", new EmployeeDTO(), br);

        assertThat(view).isEqualTo("admin/employee-edit");
        verifyNoInteractions(employeeService);
    }

    @Test
    void updateEmployee_withNewPassword_encodesAndUpdates_redirects() {
        EmployeeDTO incoming = new EmployeeDTO();
        incoming.setPassword("newRaw");
        when(passwordEncoder.encode("newRaw")).thenReturn("NEW_ENC");
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        when(employeeService.getEmployeeByEmail("e@x")).thenReturn(new EmployeeDTO()); // existing ignored in this branch

        String view = controller.updateEmployee("e@x", incoming, br);

        assertThat(view).isEqualTo("redirect:/admin/employees");
        assertThat(incoming.getPassword()).isEqualTo("NEW_ENC");
        verify(employeeService).updateEmployeeByEmail(eq("e@x"), eq(incoming));
    }

    @Test
    void updateEmployee_withEmptyPassword_usesExistingPassword_redirects() {
        EmployeeDTO incoming = new EmployeeDTO();
        incoming.setPassword(""); // empty -> should keep existing
        EmployeeDTO existing = new EmployeeDTO();
        existing.setPassword("EXISTING_HASH");
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        when(employeeService.getEmployeeByEmail("e@x")).thenReturn(existing);

        String view = controller.updateEmployee("e@x", incoming, br);

        assertThat(view).isEqualTo("redirect:/admin/employees");
        assertThat(incoming.getPassword()).isEqualTo("EXISTING_HASH");
        verify(employeeService).updateEmployeeByEmail("e@x", incoming);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deleteEmployee_callsService_andRedirects() {
        String view = controller.deleteEmployee("to@del");

        assertThat(view).isEqualTo("redirect:/admin/employees");
        verify(employeeService).deleteEmployeeByEmail("to@del");
    }

    @Test
    void viewAllOrders_withoutFilter_loadsAll_andReturnsView() {
        List<OrderDTO> orders = List.of(new OrderDTO(), new OrderDTO());
        when(orderService.getAllOrders()).thenReturn(orders);
        Model model = new ExtendedModelMap();

        String view = controller.viewAllOrders(null, model);

        assertThat(view).isEqualTo("admin/orders");
        assertThat(model.getAttribute("orders")).isEqualTo(orders);
        assertThat(model.getAttribute("statuses")).isEqualTo(OrderStatus.values());
        assertThat(model.getAttribute("selectedStatus")).isNull();
    }

    @Test
    void viewAllOrders_withFilter_loadsByStatus_andReturnsView() {
        List<OrderDTO> filtered = List.of(new OrderDTO());
        when(orderService.getOrdersByStatus(OrderStatus.PROCESSING)).thenReturn(filtered);
        Model model = new ExtendedModelMap();

        String view = controller.viewAllOrders(OrderStatus.PROCESSING, model);

        assertThat(view).isEqualTo("admin/orders");
        assertThat(model.getAttribute("orders")).isEqualTo(filtered);
        assertThat(model.getAttribute("selectedStatus")).isEqualTo(OrderStatus.PROCESSING);
    }
}
