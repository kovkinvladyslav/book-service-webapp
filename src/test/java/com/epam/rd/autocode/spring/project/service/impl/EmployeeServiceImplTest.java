package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceImplTest {

    private EmployeeRepository repo;
    private GenericMapper<Employee, EmployeeDTO> mapper;
    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(EmployeeRepository.class);
        mapper = mock(GenericMapper.class);
        service = new EmployeeServiceImpl(repo, mapper);
    }

    @Test
    void getAllEmployees_mapsList() {
        when(repo.findAll()).thenReturn(List.of(new Employee()));
        when(mapper.toDtoList(anyList())).thenReturn(List.of(new EmployeeDTO()));
        assertThat(service.getAllEmployees()).hasSize(1);
    }

    @Test
    void getEmployeeByEmail_found_maps() {
        Employee e = new Employee();
        EmployeeDTO d = new EmployeeDTO();
        when(repo.findByEmail("e")).thenReturn(Optional.of(e));
        when(mapper.toDto(e)).thenReturn(d);
        assertThat(service.getEmployeeByEmail("e")).isSameAs(d);
    }

    @Test
    void getEmployeeByEmail_notFound_throws() {
        when(repo.findByEmail("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getEmployeeByEmail("x")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void addEmployee_exists_throws() {
        EmployeeDTO dto = new EmployeeDTO(); dto.setEmail("e");
        when(repo.findByEmail("e")).thenReturn(Optional.of(new Employee()));
        assertThatThrownBy(() -> service.addEmployee(dto)).isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void addEmployee_saves_andMaps() {
        EmployeeDTO dto = new EmployeeDTO(); dto.setEmail("e");
        Employee ent = new Employee();
        Employee saved = new Employee();
        EmployeeDTO mapped = new EmployeeDTO();
        when(repo.findByEmail("e")).thenReturn(Optional.empty());
        when(mapper.toEntity(dto)).thenReturn(ent);
        when(repo.save(ent)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(mapped);
        assertThat(service.addEmployee(dto)).isSameAs(mapped);
    }

    @Test
    void updateEmployeeByEmail_notFound_throws() {
        when(repo.findByEmail("e")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateEmployeeByEmail("e", new EmployeeDTO())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateEmployeeByEmail_updates_andMaps() {
        Employee existing = new Employee();
        Employee saved = new Employee();
        EmployeeDTO dto = new EmployeeDTO();
        EmployeeDTO mapped = new EmployeeDTO();
        when(repo.findByEmail("e")).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(mapped);

        assertThat(service.updateEmployeeByEmail("e", dto)).isSameAs(mapped);
        verify(mapper).updateEntity(dto, existing);
    }

    @Test
    void deleteEmployeeByEmail_notFound_throws() {
        when(repo.findByEmail("e")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteEmployeeByEmail("e")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteEmployeeByEmail_deletes() {
        Employee e = new Employee();
        when(repo.findByEmail("e")).thenReturn(Optional.of(e));
        service.deleteEmployeeByEmail("e");
        verify(repo).delete(e);
    }
}
