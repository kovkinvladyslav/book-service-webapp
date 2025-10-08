package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.EmployeeMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final AbstractCrudService<Employee, EmployeeDTO, String> crudService;

    private static final String EMPLOYEE_NOT_FOUND = "Employee not found with email: ";
    private static final String EMPLOYEE_ALREADY_EXISTS = "Employee already exists: ";

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.crudService = new AbstractCrudService<>(
                employeeRepository,
                employeeMapper,
                email -> employeeRepository.findByEmail(email)
                        .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND + email)),
                dto -> employeeRepository.findByEmail(dto.getEmail()).isPresent(),
                dto -> EMPLOYEE_ALREADY_EXISTS + dto.getEmail()
        );
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return crudService.getAll();
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        return crudService.getByBusinessKey(email);
    }

    @Override
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO employee) {
        return crudService.update(email, employee);
    }

    @Override
    public void deleteEmployeeByEmail(String email) {
        crudService.delete(email);
    }

    @Override
    public EmployeeDTO addEmployee(EmployeeDTO employee) {
        return crudService.add(employee);
    }
}
