package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientUpdateDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Email
    private String email;
}
