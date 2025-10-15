package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import com.epam.rd.autocode.spring.project.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private ClientRepository clientRepository;
    private EmployeeRepository employeeRepository;
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        service = new CustomUserDetailsService(clientRepository, employeeRepository);
        ReflectionTestUtils.setField(service, "adminEmail", "admin@site.com");
        ReflectionTestUtils.setField(service, "adminPassword", "{noop}adminpass");
    }

    @Test
    void loadUser_admin_returnsAdminUser() {
        UserDetails ud = service.loadUserByUsername("admin@site.com");
        assertThat(ud.getUsername()).isEqualTo("admin@site.com");
        assertThat(ud.getPassword()).isEqualTo("{noop}adminpass");
        assertThat(ud.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        verifyNoInteractions(clientRepository, employeeRepository);
    }

    @Test
    void loadUser_clientFound_returnsClientUser() {
        Client c = new Client();
        c.setEmail("c@ex.com");
        c.setPassword("{noop}pwd");
        when(clientRepository.findByEmail("c@ex.com")).thenReturn(Optional.of(c));

        UserDetails ud = service.loadUserByUsername("c@ex.com");

        assertThat(ud.getUsername()).isEqualTo("c@ex.com");
        assertThat(ud.getPassword()).isEqualTo("{noop}pwd");
        assertThat(ud.getAuthorities()).extracting("authority").containsExactly("ROLE_CLIENT");
        verify(clientRepository).findByEmail("c@ex.com");
        verifyNoInteractions(employeeRepository);
    }

    @Test
    void loadUser_employeeFound_returnsEmployeeUser() {
        when(clientRepository.findByEmail("e@ex.com")).thenReturn(Optional.empty());

        Employee e = new Employee();
        e.setEmail("e@ex.com");
        e.setPassword("{noop}pwd");
        when(employeeRepository.findByEmail("e@ex.com")).thenReturn(Optional.of(e));

        UserDetails ud = service.loadUserByUsername("e@ex.com");

        assertThat(ud.getUsername()).isEqualTo("e@ex.com");
        assertThat(ud.getPassword()).isEqualTo("{noop}pwd");
        assertThat(ud.getAuthorities()).extracting("authority").containsExactly("ROLE_EMPLOYEE");
        verify(clientRepository).findByEmail("e@ex.com");
        verify(employeeRepository).findByEmail("e@ex.com");
    }

    @Test
    void loadUser_notFound_throws() {
        when(clientRepository.findByEmail("x@ex.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("x@ex.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("x@ex.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("x@ex.com");
    }
}
