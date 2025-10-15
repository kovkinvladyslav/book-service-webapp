package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    private ClientRepository clientRepository;
    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);
        service = new PaymentServiceImpl(clientRepository);
    }

    @Test
    void withdraw_notFound_throws() {
        when(clientRepository.findByEmail("e")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.withdraw("e", BigDecimal.ONE)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void withdraw_nullBalance_initializedThenChecked() {
        Client c = new Client(); c.setBalance(null);
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        assertThatThrownBy(() -> service.withdraw("e", BigDecimal.ONE)).isInstanceOf(IllegalStateException.class);
        assertThat(c.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void withdraw_insufficient_throws() {
        Client c = new Client(); c.setBalance(new BigDecimal("2"));
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        assertThatThrownBy(() -> service.withdraw("e", new BigDecimal("5"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void withdraw_ok_updatesBalance() {
        Client c = new Client(); c.setBalance(new BigDecimal("10"));
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        service.withdraw("e", new BigDecimal("3"));
        assertThat(c.getBalance()).isEqualByComparingTo(new BigDecimal("7"));
        verify(clientRepository).save(c);
    }

    @Test
    void deposit_notFound_throws() {
        when(clientRepository.findByEmail("e")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deposit("e", BigDecimal.ONE)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void deposit_initializesNullBalance_andAdds() {
        Client c = new Client(); c.setBalance(null);
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        service.deposit("e", new BigDecimal("4"));
        assertThat(c.getBalance()).isEqualByComparingTo(new BigDecimal("4"));
        verify(clientRepository).save(c);
    }

    @Test
    void deposit_addsToExisting() {
        Client c = new Client(); c.setBalance(new BigDecimal("6"));
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        service.deposit("e", new BigDecimal("2"));
        assertThat(c.getBalance()).isEqualByComparingTo(new BigDecimal("8"));
    }

    @Test
    void processPayment_success_returnsTrue() {
        Client c = new Client(); c.setBalance(new BigDecimal("5"));
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        assertThat(service.processPayment("e", new BigDecimal("3"))).isTrue();
    }

    @Test
    void processPayment_insufficient_returnsFalse() {
        Client c = new Client(); c.setBalance(new BigDecimal("1"));
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        assertThat(service.processPayment("e", new BigDecimal("3"))).isFalse();
    }
}
