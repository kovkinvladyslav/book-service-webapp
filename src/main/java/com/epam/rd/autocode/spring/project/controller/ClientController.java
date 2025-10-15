package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientController {
    private final PaymentService paymentService;
    private final ClientService clientService;

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication auth) {
        ClientDTO client = clientService.getClientByEmail(auth.getName());
        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setName(client.getName());
        updateDTO.setEmail(client.getEmail());
        model.addAttribute("client", updateDTO);
        model.addAttribute("balance", client.getBalance());
        return "client/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("client") ClientUpdateDTO updatedClient,
                                BindingResult result,
                                Authentication auth,
                                Model model) {

        String email = auth.getName();

        if (result.hasErrors()) {
            ClientDTO current = clientService.getClientByEmail(email);
            model.addAttribute("balance", current.getBalance());
            return "client/profile";
        }

        clientService.updateClientByEmail(email, updatedClient);
        ClientDTO updated = clientService.getClientByEmail(email);
        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setEmail(updated.getEmail());
        dto.setName(updated.getName());
        model.addAttribute("client", dto);
        model.addAttribute("balance", updated.getBalance());
        model.addAttribute("successMessage", "Profile updated successfully!");
        return "client/profile";
    }

    @GetMapping("/balance")
    public String showBalance(Authentication auth, Model model) {
        var client = clientService.getClientByEmail(auth.getName());
        model.addAttribute("client", client);
        return "client/balance";
    }

    @PostMapping("/deposit") public String depositFunds(@RequestParam BigDecimal amount, Authentication auth) {
        paymentService.deposit(auth.getName(), amount);
        return "redirect:/client/balance";
    }



}
