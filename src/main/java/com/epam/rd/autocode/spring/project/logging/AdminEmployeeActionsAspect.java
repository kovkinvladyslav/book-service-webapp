package com.epam.rd.autocode.spring.project.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AdminEmployeeActionsAspect {

    @After("execution(* com.epam.rd.autocode.spring.project.controller.AdminController.addEmployee(..)) && args(..)")
    public void adminAddEmployee() { log.info("[ADMIN] add employee"); }

    @After("execution(* com.epam.rd.autocode.spring.project.controller.AdminController.updateEmployee(..)) && args(email, ..)")
    public void adminUpdateEmployee(String email) { log.info("[ADMIN] update employee {}", LogSanitizer.maskEmail(email)); }

    @After("execution(* com.epam.rd.autocode.spring.project.controller.AdminController.deleteEmployee(..)) && args(email)")
    public void adminDeleteEmployee(String email) { log.warn("[ADMIN] delete employee {}", LogSanitizer.maskEmail(email)); }

    @After("execution(* com.epam.rd.autocode.spring.project.controller.EmployeeController.acceptOrder(..)) && args(id, ..)")
    public void employeeAccept(Long id) { log.info("[EMPLOYEE] accept order {}", id); }

    @After("execution(* com.epam.rd.autocode.spring.project.controller.EmployeeController.completeOrder(..)) && args(id)")
    public void employeeComplete(Long id) { log.info("[EMPLOYEE] complete order {}", id); }
}
