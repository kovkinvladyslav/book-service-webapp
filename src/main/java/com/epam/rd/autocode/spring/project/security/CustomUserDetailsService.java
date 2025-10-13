package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import com.epam.rd.autocode.spring.project.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        var employee = employeeRepository.findByEmail(email);
        if (employee.isPresent()) {
            return User.withUsername(email)
                    .password(employee.get().getPassword())
                    .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
                    .build();
        }

        var client = clientRepository.findByEmail(email);
        if (client.isPresent()) {
            return User.withUsername(email)
                    .password(client.get().getPassword())
                    .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))
                    .build();
        }

        throw new UsernameNotFoundException("User not found: " + email);
    }
}
