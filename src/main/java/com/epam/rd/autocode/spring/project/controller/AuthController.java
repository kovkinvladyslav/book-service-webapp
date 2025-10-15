package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@Controller
@RequiredArgsConstructor
public class AuthController {

    private final ClientService clientService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeService employeeService;


    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String registered,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (registered != null) {
            model.addAttribute("success", "Registration successful! Please login.");
        }
        if (logout != null) {
            model.addAttribute("info", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(@RequestParam(required = false) String error,
                                   Model model) {
        model.addAttribute("clientDTO", new ClientDTO());

        if ("exists".equals(error)) {
            model.addAttribute("error", "This email is already registered. Please login or use another email.");
        }

        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute ClientDTO clientDTO,
                           BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        clientDTO.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        if (clientDTO.getBalance() == null) {
            clientDTO.setBalance(BigDecimal.ZERO);
        }

        clientService.addClient(clientDTO);
        return "redirect:/login?registered";
    }

    @GetMapping("/")
    public String index(Authentication auth, Model model) {
        if (auth != null) {
            String email = auth.getName();
            String displayName = email;

            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
                displayName = clientService.getClientByEmail(email).getName();
            } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"))) {
                displayName = employeeService.getEmployeeByEmail(email).getName();
            }

            model.addAttribute("displayName", displayName);
        }

        return "index";
    }
}