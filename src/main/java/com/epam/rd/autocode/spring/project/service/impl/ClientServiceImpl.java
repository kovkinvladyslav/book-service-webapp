package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    private static final String CLIENT_NOT_FOUND = "Client not found with email: ";
    private static final String CLIENT_ALREADY_EXISTS = "Client already exists: ";

    public ClientServiceImpl(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

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
            throw new AlreadyExistException(CLIENT_ALREADY_EXISTS + dto.getEmail());
        }
        Client client = clientMapper.toEntity(dto);
        return clientMapper.toDto(clientRepository.save(client));
    }

    @Override
    public ClientDTO updateClientByEmail(String email, ClientDTO dto) {
        Client existing = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND + email));

        clientMapper.updateEntity(dto, existing);
        return clientMapper.toDto(clientRepository.save(existing));
    }

    @Override
    public void deleteClientByEmail(String email) {
        Client existing = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND + email));
        clientRepository.delete(existing);
    }
}
