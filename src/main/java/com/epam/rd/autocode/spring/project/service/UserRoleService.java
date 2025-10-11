package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.enums.UserRole;

public interface UserRoleService {
    UserRole getUserRole(String email);
}
