package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final EmployeeRepository employees;
    private final PasswordEncoder encoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.name}")
    private String adminName;

    @Value("${admin.password-plain}")
    private String adminPasswordPlain;

    @Override
    public void run(String... args) {
        employees.findByEmail(adminEmail).ifPresentOrElse(
                existing -> {
                    existing.setName(adminName);
                    existing.setPassword(encoder.encode(adminPasswordPlain));
                    employees.save(existing);
                },
                () -> {
                    Employee admin = new Employee();
                    admin.setEmail(adminEmail);
                    admin.setName(adminName);
                    admin.setPassword(encoder.encode(adminPasswordPlain));
                    employees.save(admin);
                }
        );
    }
}
