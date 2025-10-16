package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GenericMapper<Employee, EmployeeDTO> employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private final String adminEmail = "admin@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(employeeService, "adminEmail", adminEmail);
    }

    @Test
    void getAllEmployees_filtersOutAdmin() {
        Employee admin = new Employee();
        admin.setEmail(adminEmail);
        Employee e1 = new Employee();
        e1.setEmail("user1@example.com");
        EmployeeDTO dto = new EmployeeDTO();

        when(employeeRepository.findAll()).thenReturn(List.of(admin, e1));
        when(employeeMapper.toDto(e1)).thenReturn(dto);

        List<EmployeeDTO> result = employeeService.getAllEmployees();

        assertThat(result).containsExactly(dto);
        verify(employeeRepository).findAll();
    }

    @Test
    void getEmployeeByEmail_returnsDto() {
        Employee e = new Employee();
        e.setEmail("user@example.com");
        EmployeeDTO dto = new EmployeeDTO();

        when(employeeRepository.findByEmail("user@example.com")).thenReturn(Optional.of(e));
        when(employeeMapper.toDto(e)).thenReturn(dto);

        EmployeeDTO result = employeeService.getEmployeeByEmail("user@example.com");

        assertThat(result).isEqualTo(dto);
        verify(employeeRepository).findByEmail("user@example.com");
    }

    @Test
    void getEmployeeByEmail_throwsIfAdmin() {
        assertThatThrownBy(() -> employeeService.getEmployeeByEmail(adminEmail))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Admin account cannot be modified or deleted");
    }

    @Test
    void getEmployeeByEmail_throwsIfNotFound() {
        when(employeeRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeByEmail("missing@example.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Employee not found with email: missing@example.com");
    }

    @Test
    void addEmployee_savesAndReturnsDto() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("new@example.com");
        Employee e = new Employee();

        when(employeeRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(dto)).thenReturn(e);
        when(employeeRepository.save(e)).thenReturn(e);
        when(employeeMapper.toDto(e)).thenReturn(dto);

        EmployeeDTO result = employeeService.addEmployee(dto);

        assertThat(result).isEqualTo(dto);
        verify(employeeRepository).save(e);
    }

    @Test
    void addEmployee_throwsIfAdmin() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail(adminEmail);

        assertThatThrownBy(() -> employeeService.addEmployee(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot create admin account manually");
    }

    @Test
    void addEmployee_throwsIfAlreadyExists() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("exists@example.com");
        when(employeeRepository.findByEmail("exists@example.com")).thenReturn(Optional.of(new Employee()));

        assertThatThrownBy(() -> employeeService.addEmployee(dto))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("Employee already exists: exists@example.com");
    }

    @Test
    void updateEmployeeByEmail_updatesAndReturnsDto() {
        Employee existing = new Employee();
        EmployeeDTO dto = new EmployeeDTO();
        when(employeeRepository.findByEmail("x@example.com")).thenReturn(Optional.of(existing));
        when(employeeRepository.save(existing)).thenReturn(existing);
        when(employeeMapper.toDto(existing)).thenReturn(dto);

        EmployeeDTO result = employeeService.updateEmployeeByEmail("x@example.com", dto);

        assertThat(result).isEqualTo(dto);
        verify(employeeMapper).updateEntity(dto, existing);
    }

    @Test
    void updateEmployeeByEmail_throwsIfAdmin() {
        assertThatThrownBy(() -> employeeService.updateEmployeeByEmail(adminEmail, new EmployeeDTO()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Admin account cannot be modified or deleted");
    }

    @Test
    void updateEmployeeByEmail_throwsIfNotFound() {
        when(employeeRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployeeByEmail("missing@example.com", new EmployeeDTO()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Employee not found with email: missing@example.com");
    }


    @Test
    void deleteEmployeeByEmail_deletesExisting() {
        Employee existing = new Employee();
        when(employeeRepository.findByEmail("x@example.com")).thenReturn(Optional.of(existing));

        employeeService.deleteEmployeeByEmail("x@example.com");

        verify(employeeRepository).delete(existing);
    }

    @Test
    void deleteEmployeeByEmail_throwsIfAdmin() {
        assertThatThrownBy(() -> employeeService.deleteEmployeeByEmail(adminEmail))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Admin account cannot be modified or deleted");
    }

    @Test
    void deleteEmployeeByEmail_throwsIfNotFound() {
        when(employeeRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployeeByEmail("missing@example.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Employee not found with email: missing@example.com");
    }
}
