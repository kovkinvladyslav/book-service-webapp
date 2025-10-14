package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeController {

    private final OrderService orderService;
    private final EmployeeService employeeService;

    @GetMapping("/orders")
    public String viewOrders(Authentication auth, Model model) {
        String email = auth.getName();
        model.addAttribute("employee", employeeService.getEmployeeByEmail(email));

        model.addAttribute("myOrders", orderService.getOrdersByEmployee(email));

        model.addAttribute("newOrders", orderService.getOrdersByStatus(OrderStatus.PLACED));

        return "employee/manage-orders";
    }

    @PostMapping("/accept/{id}")
    public String acceptOrder(@PathVariable Long id, Authentication auth) {
        orderService.assignOrderToEmployee(id, auth.getName());

        return "redirect:/orders/manage-orders";
    }

    @PostMapping("/complete/{id}")
    public String completeOrder(@PathVariable Long id) {
        orderService.markOrderAsCompleted(id);
        return "redirect:/orders/manage-orders";
    }


    @GetMapping("/profile")
    public String profile(Model model, Authentication auth) {
        var employee = employeeService.getEmployeeByEmail(auth.getName());
        model.addAttribute("employee", employee);
        return "employee/profile";
    }
}
