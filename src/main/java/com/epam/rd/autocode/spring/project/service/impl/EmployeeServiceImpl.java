package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repository.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final GenericMapper<Employee, EmployeeDTO> employeeMapper;

    @Value("${admin.email}")
    private String adminEmail;

    private static final String EMPLOYEE_NOT_FOUND = "Employee not found with email: ";
    private static final String EMPLOYEE_ALREADY_EXISTS = "Employee already exists: ";
    private static final String ADMIN_PROTECTED = "Admin account cannot be modified or deleted";

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .filter(emp -> emp.getEmail() != null && !emp.getEmail().equals(adminEmail))
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        if (email.equals(adminEmail)) {
            throw new IllegalStateException(ADMIN_PROTECTED);
        }

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND + email));
        return employeeMapper.toDto(employee);
    }

    @Override
    public EmployeeDTO addEmployee(EmployeeDTO dto) {
        if (dto.getEmail().equals(adminEmail)) {
            throw new IllegalStateException("Cannot create admin account manually");
        }

        if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new AlreadyExistException(EMPLOYEE_ALREADY_EXISTS + dto.getEmail());
        }

        Employee employee = employeeMapper.toEntity(dto);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO dto) {
        if (email.equals(adminEmail)) {
            throw new IllegalStateException(ADMIN_PROTECTED);
        }

        Employee existing = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND + email));

        employeeMapper.updateEntity(dto, existing);
        return employeeMapper.toDto(employeeRepository.save(existing));
    }

    @Override
    public void deleteEmployeeByEmail(String email) {
        if (email.equals(adminEmail)) {
            throw new IllegalStateException(ADMIN_PROTECTED);
        }

        Employee existing = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND + email));
        employeeRepository.delete(existing);
    }
}
