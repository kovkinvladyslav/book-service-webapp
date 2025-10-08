package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {
    private final AbstractCrudService<Client, ClientDTO, String> crudService;

    private static final String CLIENT_NOT_FOUND = "Client not found with email: ";
    private static final String CLIENT_ALREADY_EXISTS = "Client already exists: ";

    public ClientServiceImpl(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.crudService = new AbstractCrudService<>(
                clientRepository,
                clientMapper,
                email -> clientRepository.findByEmail(email)
                        .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND + email)),
                dto -> clientRepository.findByEmail(dto.getEmail()).isPresent(),
                dto -> CLIENT_ALREADY_EXISTS + dto.getEmail()
        );
    }

    @Override
    public List<ClientDTO> getAllClients() {
        return crudService.getAll();
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        return crudService.getByBusinessKey(email);
    }

    @Override
    public ClientDTO updateClientByEmail(String email, ClientDTO client) {
        return crudService.update(email, client);
    }

    @Override
    public void deleteClientByEmail(String email) {
        crudService.delete(email);
    }

    @Override
    public ClientDTO addClient(ClientDTO client) {
        return crudService.add(client);
    }
}
