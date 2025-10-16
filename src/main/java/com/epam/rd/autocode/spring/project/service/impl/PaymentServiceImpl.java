package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import com.epam.rd.autocode.spring.project.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public void withdraw(String clientEmail, BigDecimal amount) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found: " + clientEmail));

        if (client.getBalance() == null) {
            client.setBalance(BigDecimal.ZERO);
        }

        if (client.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance. Please top up your account.");
        }

        client.setBalance(client.getBalance().subtract(amount));
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public void deposit(String clientEmail, BigDecimal amount) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found: " + clientEmail));

        if (client.getBalance() == null) {
            client.setBalance(BigDecimal.ZERO);
        }

        client.setBalance(client.getBalance().add(amount));
        clientRepository.save(client);
    }

    @Transactional
    @Override
    public boolean processPayment(String clientEmail, BigDecimal amount) {
        try {
            withdraw(clientEmail, amount);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
