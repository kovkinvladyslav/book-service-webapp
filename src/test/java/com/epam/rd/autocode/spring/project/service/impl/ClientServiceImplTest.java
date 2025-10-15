package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.UserAlreadyExists;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientServiceImplTest {

    private ClientRepository clientRepository;
    private GenericMapper<Client, ClientDTO> mapper;
    private ClientServiceImpl service;

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);
        mapper = mock(GenericMapper.class);
        service = new ClientServiceImpl(clientRepository, mapper);
    }

    @Test
    void getAllClients_mapsList() {
        when(clientRepository.findAll()).thenReturn(List.of(new Client()));
        when(mapper.toDtoList(anyList())).thenReturn(List.of(new ClientDTO()));
        assertThat(service.getAllClients()).hasSize(1);
    }

    @Test
    void getClientByEmail_found_maps() {
        Client c = new Client();
        ClientDTO d = new ClientDTO();
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        when(mapper.toDto(c)).thenReturn(d);
        assertThat(service.getClientByEmail("e")).isSameAs(d);
    }

    @Test
    void updateClientByEmail_nullPassword_keepsExisting_andSaves() {
        Client existing = new Client();
        existing.setPassword("oldHash");
        when(clientRepository.findByEmail("e")).thenReturn(java.util.Optional.of(existing));

        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setName("New Name");
        dto.setPassword(null);
        service.updateClientByEmail("e", dto);

        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getPassword()).isEqualTo("oldHash");
        verify(clientRepository).save(existing);
    }


    @Test
    void getClientByEmail_notFound_throws() {
        when(clientRepository.findByEmail("e")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getClientByEmail("e")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void addClient_whenExists_throws() {
        ClientDTO dto = new ClientDTO(); dto.setEmail("e");
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(new Client()));
        assertThatThrownBy(() -> service.addClient(dto)).isInstanceOf(UserAlreadyExists.class);
    }

    @Test
    void addClient_saves_andMaps() {
        ClientDTO dto = new ClientDTO(); dto.setEmail("e");
        Client ent = new Client();
        Client saved = new Client();
        ClientDTO mapped = new ClientDTO();
        when(clientRepository.findByEmail("e")).thenReturn(Optional.empty());
        when(mapper.toEntity(dto)).thenReturn(ent);
        when(clientRepository.save(ent)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(mapped);
        assertThat(service.addClient(dto)).isSameAs(mapped);
    }

    @Test
    void deposit_notFound_throws() {
        when(clientRepository.findByEmail("e")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deposit("e", BigDecimal.TEN)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void deposit_nonPositive_throws() {
        Client c = new Client(); c.setBalance(BigDecimal.ZERO);
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        assertThatThrownBy(() -> service.deposit("e", BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deposit_nullBalance_initializedAndAdded() {
        Client c = new Client(); c.setBalance(null);
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        service.deposit("e", new BigDecimal("5"));
        assertThat(c.getBalance()).isEqualByComparingTo(new BigDecimal("5"));
        verify(clientRepository).save(c);
    }

    @Test
    void deposit_addsToExistingBalance() {
        Client c = new Client(); c.setBalance(new BigDecimal("7"));
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        service.deposit("e", new BigDecimal("3"));
        assertThat(c.getBalance()).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    void updateClientByEmail_notFound_throws() {
        when(clientRepository.findByEmail("e")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateClientByEmail("e", new ClientUpdateDTO())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateClientByEmail_blankPassword_keepsExisting() {
        Client existing = new Client();
        existing.setPassword("old");
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(existing));

        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setName("N");
        dto.setPassword("   ");

        service.updateClientByEmail("e", dto);

        assertThat(existing.getName()).isEqualTo("N");
        assertThat(existing.getPassword()).isEqualTo("old");
        verify(clientRepository).save(existing);
    }

    @Test
    void updateClientByEmail_setsNewPasswordWhenProvided() {
        Client existing = new Client();
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(existing));

        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setName("N2");
        dto.setPassword("new");

        service.updateClientByEmail("e", dto);

        assertThat(existing.getName()).isEqualTo("N2");
        assertThat(existing.getPassword()).isEqualTo("new");
    }

    @Test
    void deleteClientByEmail_notFound_throws() {
        when(clientRepository.findByEmail("e")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteClientByEmail("e")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteClientByEmail_deletes() {
        Client c = new Client();
        when(clientRepository.findByEmail("e")).thenReturn(Optional.of(c));
        service.deleteClientByEmail("e");
        verify(clientRepository).delete(c);
    }
}
