package com.epam.rd.autocode.spring.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
public class EmployeeDTO{
    private String email;
    private String name;
    private String password;
    private String phone;
    private LocalDate birthDate;
}
