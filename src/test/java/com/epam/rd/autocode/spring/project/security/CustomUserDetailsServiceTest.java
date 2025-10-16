package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import com.epam.rd.autocode.spring.project.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private final String adminEmail = "admin@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        try {
            var field = CustomUserDetailsService.class.getDeclaredField("adminEmail");
            field.setAccessible(true);
            field.set(userDetailsService, adminEmail);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void loadUserByUsername_ClientFound() {
        Client client = new Client();
        client.setEmail("client@example.com");
        client.setPassword("password");

        when(clientRepository.findByEmail("client@example.com")).thenReturn(Optional.of(client));

        UserDetails userDetails = userDetailsService.loadUserByUsername("client@example.com");

        assertEquals("client@example.com", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")));
        verify(clientRepository, times(1)).findByEmail("client@example.com");
        verifyNoInteractions(employeeRepository);
    }

    @Test
    void loadUserByUsername_EmployeeFound() {
        Employee employee = new Employee();
        employee.setEmail("employee@example.com");
        employee.setPassword("password");

        when(clientRepository.findByEmail("employee@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("employee@example.com")).thenReturn(Optional.of(employee));

        UserDetails userDetails = userDetailsService.loadUserByUsername("employee@example.com");

        assertEquals("employee@example.com", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")));
    }

    @Test
    void loadUserByUsername_AdminFound() {
        Employee employee = new Employee();
        employee.setEmail(adminEmail);
        employee.setPassword("password");

        when(clientRepository.findByEmail(adminEmail)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(adminEmail)).thenReturn(Optional.of(employee));

        UserDetails userDetails = userDetailsService.loadUserByUsername(adminEmail);

        assertEquals(adminEmail, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        when(clientRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown@example.com"));
    }
}
