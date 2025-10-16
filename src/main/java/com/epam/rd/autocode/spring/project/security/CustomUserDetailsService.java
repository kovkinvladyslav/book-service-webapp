package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import com.epam.rd.autocode.spring.project.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    @Value("${admin.email}")
    private String adminEmail;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        return clientRepository.findByEmail(email)
                .map(client -> User.builder()
                        .username(client.getEmail())
                        .password(client.getPassword())
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .build())
                .or(() -> employeeRepository.findByEmail(email)
                        .map(employee -> {
                            String role = email.equals(adminEmail)
                                    ? "ROLE_ADMIN"
                                    : "ROLE_EMPLOYEE";

                            return User.builder()
                                    .username(employee.getEmail())
                                    .password(employee.getPassword())
                                    .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                                    .build();
                        }))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}