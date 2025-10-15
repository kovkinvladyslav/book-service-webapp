package com.epam.rd.autocode.spring.project.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Public routes
                        .requestMatchers("/login", "/register").anonymous()
                        .requestMatchers("/", "/css/**", "/js/**").permitAll()

                        // ADMIN routes (most specific first)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers("/books/manage/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/books/*/edit").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/books/*/delete").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/books/*/restore").hasAnyRole("EMPLOYEE", "ADMIN")

                        .requestMatchers("/employee/**").hasAnyRole("EMPLOYEE", "ADMIN")

                        .requestMatchers("/books/**").permitAll()
                        .requestMatchers("/books").permitAll()

                        .requestMatchers("/orders/**").hasAnyRole("CLIENT", "EMPLOYEE", "ADMIN")

                        .requestMatchers("/client/**").hasRole("CLIENT")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((req, res, ex1) -> res.sendRedirect("/"))
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}