package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ClientService {

    List<ClientDTO> getAllClients();

    ClientDTO getClientByEmail(String email);

    void updateClientByEmail(String email, ClientUpdateDTO client);

    void deleteClientByEmail(String email);

    ClientDTO addClient(ClientDTO client);

    void deposit(String name, BigDecimal amount);
}
