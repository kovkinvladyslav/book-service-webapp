package com.epam.rd.autocode.spring.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ClientDTO{
    private String email;
    private String name;
    private String password;
    private BigDecimal balance;
}
