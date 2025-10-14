package com.epam.rd.autocode.spring.project.controller;


import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/add")
    public String addToCart(@RequestParam String name, Authentication authentication) {
        orderService.addBookToOrder(name, authentication.getName());
        return "redirect:/orders/cart";
    }
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam String name, Authentication authentication) {
        orderService.removeBookFromOrder(name, authentication.getName());
        return "redirect:/orders/cart";
    }

    @GetMapping("/cart")
    @PreAuthorize("hasRole('CLIENT')")
    public String viewCart(Authentication auth, Model model) {
        OrderDTO draft = orderService.getOrdersByClient(auth.getName()).stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.DRAFT)
                .findFirst()
                .orElse(null);

        model.addAttribute("order", draft);
        return "client/cart";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/checkout")
    public String placeOrder(Authentication auth) {
        orderService.placeOrder(auth.getName());
        return "redirect:/orders";
    }

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    public String listClientOrders(Authentication auth, Model model) {
        List<OrderDTO> orders = orderService.getOrdersByClient(auth.getName()).stream()
                .filter(orderDTO -> !orderDTO.getOrderStatus().equals(OrderStatus.DRAFT)).toList();
        model.addAttribute("orders", orders);
        return "client/orders";
    }

    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public String cancelOrder(@PathVariable Long id, Authentication auth) {
        orderService.cancelOrder(auth.getName(), id);
        return "redirect:/orders";
    }

}
