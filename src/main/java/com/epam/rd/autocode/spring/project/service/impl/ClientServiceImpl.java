package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.UserAlreadyExists;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@RequiredArgsConstructor
@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final GenericMapper<Client, ClientDTO> clientMapper;

    private static final String CLIENT_NOT_FOUND = "Client not found with email: ";
    private static final String CLIENT_ALREADY_EXISTS = "Client already exists: ";

    @Override
    public List<ClientDTO> getAllClients() {
        return clientMapper.toDtoList(clientRepository.findAll());
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND + email));
        return clientMapper.toDto(client);
    }

    @Override
    public ClientDTO addClient(ClientDTO dto) {
        if (clientRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new UserAlreadyExists(CLIENT_ALREADY_EXISTS + dto.getEmail());
        }
        Client client = clientMapper.toEntity(dto);
        return clientMapper.toDto(clientRepository.save(client));
    }

    @Override
    @Transactional
    public void deposit(String clientEmail, BigDecimal amount) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found: " + clientEmail));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        if (client.getBalance() == null) {
            client.setBalance(BigDecimal.ZERO);
        }

        client.setBalance(client.getBalance().add(amount));
        clientRepository.save(client);
    }


    @Override
    @Transactional
    public void updateClientByEmail(String email, ClientUpdateDTO dto) {
        Client existing = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND + email));

        existing.setName(dto.getName());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(dto.getPassword());
        }

        clientRepository.save(existing);
    }




    @Override
    public void deleteClientByEmail(String email) {
        Client existing = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND + email));
        clientRepository.delete(existing);
    }
}
