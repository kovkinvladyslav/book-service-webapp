package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EmployeeService employeeService;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.name}")
    private String adminName;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("adminName", adminName);
        model.addAttribute("employeeCount", employeeService.getAllEmployees().size());

        List<OrderDTO> allOrders = orderService.getAllOrders();
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("placedOrders", allOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.PLACED).count());
        model.addAttribute("processingOrders", allOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.PROCESSING).count());
        model.addAttribute("completedOrders", allOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED).count());

        return "admin/dashboard";
    }

    @GetMapping("/employees")
    public String listEmployees(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "admin/employees";
    }

    @GetMapping("/employees/add")
    public String showAddEmployeeForm(Model model) {
        model.addAttribute("employeeDTO", new EmployeeDTO());
        return "admin/employee-add";
    }

    @PostMapping("/employees/add")
    public String addEmployee(@Valid @ModelAttribute EmployeeDTO employeeDTO,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/employee-add";
        }

        employeeDTO.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        employeeService.addEmployee(employeeDTO);

        return "redirect:/admin/employees";
    }

    @GetMapping("/employees/{email}/edit")
    public String showEditEmployeeForm(@PathVariable String email, Model model) {
        EmployeeDTO employee = employeeService.getEmployeeByEmail(email);
        model.addAttribute("employeeDTO", employee);
        return "admin/employee-edit";
    }

    @PostMapping("/employees/{email}/edit")
    public String updateEmployee(@PathVariable String email,
                                 @Valid @ModelAttribute EmployeeDTO employeeDTO,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/employee-edit";
        }

        EmployeeDTO existing = employeeService.getEmployeeByEmail(email);

        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().isEmpty()) {
            employeeDTO.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        } else {
            employeeDTO.setPassword(existing.getPassword());
        }

        employeeService.updateEmployeeByEmail(email, employeeDTO);

        return "redirect:/admin/employees";
    }

    @PostMapping("/employees/{email}/delete")
    public String deleteEmployee(@PathVariable String email) {
        employeeService.deleteEmployeeByEmail(email);
        return "redirect:/admin/employees";
    }

    @GetMapping("/orders")
    public String viewAllOrders(@RequestParam(required = false) OrderStatus status,
                                Model model) {
        List<OrderDTO> orders;

        if (status != null) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);

        return "admin/orders";
    }
}