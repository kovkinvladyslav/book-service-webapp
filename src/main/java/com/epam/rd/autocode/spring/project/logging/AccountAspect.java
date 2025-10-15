package com.epam.rd.autocode.spring.project.logging;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AccountAspect {

    @After("execution(* com.epam.rd.autocode.spring.project.service.ClientService.addClient(..)) && args(dto)")
    public void clientCreated(ClientDTO dto) {
        log.info("[CLIENT] created email={}", dto != null ? LogSanitizer.maskEmail(dto.getEmail()) : "null");
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.ClientService.updateClientByEmail(..)) && args(email, dto)")
    public void clientUpdated(String email, ClientUpdateDTO dto) {
        boolean pwdChanged = dto != null && dto.getPassword() != null && !dto.getPassword().isBlank();
        log.info("[CLIENT] updated email={} newName={} passwordChanged={}",
                LogSanitizer.maskEmail(email), dto != null ? LogSanitizer.mask(dto.getName()) : "null", pwdChanged);
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.ClientService.deleteClientByEmail(..)) && args(email)")
    public void clientDeleted(String email) {
        log.warn("[CLIENT] deleted email={}", LogSanitizer.maskEmail(email));
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.EmployeeService.addEmployee(..)) && args(dto)")
    public void employeeCreated(EmployeeDTO dto) {
        log.info("[EMPLOYEE] created email={} name={}",
                dto != null ? LogSanitizer.maskEmail(dto.getEmail()) : "null",
                dto != null ? LogSanitizer.mask(dto.getName()) : "null");
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.EmployeeService.updateEmployeeByEmail(..)) && args(email, ..)")
    public void employeeUpdated(String email) {
        log.info("[EMPLOYEE] updated email={}", LogSanitizer.maskEmail(email));
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.EmployeeService.deleteEmployeeByEmail(..)) && args(email)")
    public void employeeDeleted(String email) {
        log.warn("[EMPLOYEE] deleted email={}", LogSanitizer.maskEmail(email));
    }
}
