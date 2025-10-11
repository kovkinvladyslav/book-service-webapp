package com.epam.rd.autocode.spring.project.dto;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClientDTO{
    private String email;
    private String name;
    private String password;
    private BigDecimal balance;
}
