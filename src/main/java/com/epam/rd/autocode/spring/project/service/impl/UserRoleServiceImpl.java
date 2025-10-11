package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.enums.UserRole;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;

    @Override
    public UserRole getUserRole(String email) {
        if (employeeRepository.findByEmail(email).isPresent()) {
            return UserRole.EMPLOYEE;
        }

        if (clientRepository.findByEmail(email).isPresent()) {
            return UserRole.CLIENT;
        }

        throw new NotFoundException("User " + email + " is not registered");
    }
}
