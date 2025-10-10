package com.epam.rd.autocode.spring.project.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "employees")
@Getter
@Setter
public class Employee extends User{
    private String phone;
    private LocalDate birthDate;
}
