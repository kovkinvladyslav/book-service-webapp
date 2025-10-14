package com.epam.rd.autocode.spring.project.controller;


import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/client")
@PreAuthorize("hasRole('CLIENT')")
public class ClientController {
    private final ClientService clientService;

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        model.addAttribute("client", clientService.getClientByEmail(authentication.getName()));
        return "client/profile";
    }

    @GetMapping("/balance")
    public String showBalance(Authentication auth, Model model) {
        var client = clientService.getClientByEmail(auth.getName());
        model.addAttribute("client", client);
        return "client/balance";
    }

    @PostMapping("/deposit") public String depositFunds(@RequestParam BigDecimal amount, Authentication auth) {
        clientService.deposit(auth.getName(), amount);
        return "redirect:/client/balance";
    }

}
