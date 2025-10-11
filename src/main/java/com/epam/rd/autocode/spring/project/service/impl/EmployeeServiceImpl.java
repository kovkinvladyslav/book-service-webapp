package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final GenericMapper<Employee, EmployeeDTO> employeeMapper;

    private static final String EMPLOYEE_NOT_FOUND = "Employee not found with email: ";
    private static final String EMPLOYEE_ALREADY_EXISTS = "Employee already exists: ";

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeMapper.toDtoList(employeeRepository.findAll());
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND + email));
        return employeeMapper.toDto(employee);
    }

    @Override
    public EmployeeDTO addEmployee(EmployeeDTO dto) {
        if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new AlreadyExistException(EMPLOYEE_ALREADY_EXISTS + dto.getEmail());
        }
        Employee employee = employeeMapper.toEntity(dto);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO dto) {
        Employee existing = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND + email));

        employeeMapper.updateEntity(dto, existing);
        return employeeMapper.toDto(employeeRepository.save(existing));
    }

    @Override
    public void deleteEmployeeByEmail(String email) {
        Employee existing = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND + email));
        employeeRepository.delete(existing);
    }
}
