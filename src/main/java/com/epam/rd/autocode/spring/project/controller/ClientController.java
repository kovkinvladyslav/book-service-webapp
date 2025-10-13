package com.epam.rd.autocode.spring.project.controller;


import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientController {
    private final ClientService clientService;
    private final OrderService orderService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CLIENT')")
    public String dashboard(Model model, Authentication authentication) {
        var email = authentication.getName();
        model.addAttribute("client", clientService.getClientByEmail(email));
        model.addAttribute("orders", orderService.getOrdersByClient(email));
        return "client/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        model.addAttribute("client", clientService.getClientByEmail(authentication.getName()));
        return "client/profile";
    }
}
