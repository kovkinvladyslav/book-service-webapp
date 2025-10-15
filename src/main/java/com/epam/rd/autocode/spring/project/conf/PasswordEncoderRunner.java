package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repository.ClientRepository;
import com.epam.rd.autocode.spring.project.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
@Profile("test")
@RequiredArgsConstructor
@Slf4j
public class PasswordEncoderRunner implements CommandLineRunner {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        encodePasswords(
                clientRepository.findAll(),
                clientRepository::save,
                "CLIENT"
        );

        encodePasswords(
                employeeRepository.findAll(),
                employeeRepository::save,
                "EMPLOYEE"
        );
    }

    private <T extends User> void encodePasswords(
            List<T> users,
            Consumer<T> saveFunction,
            String userType) {

        users.stream()
                .filter(user -> !isAlreadyEncoded(user.getPassword()))
                .forEach(user -> {
                    String originalPassword = user.getPassword();
                    user.setPassword(passwordEncoder.encode(originalPassword));
                    saveFunction.accept(user);
                });
    }

    private boolean isAlreadyEncoded(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$");
    }
}