package com.epam.rd.autocode.spring.project.logging;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AuthAspect {

    @After("execution(* com.epam.rd.autocode.spring.project.controller.AuthController.register(..)) && args(clientDTO, ..)")
    public void onRegister(ClientDTO clientDTO) {
        log.info("[AUTH] register email={}", clientDTO != null ? LogSanitizer.maskEmail(clientDTO.getEmail()) : "null");
    }

    @After("execution(* com.epam.rd.autocode.spring.project.controller.ClientController.updateProfile(..)) && args(updatedClient, ..)")
    public void onProfileUpdate(Object updatedClient) {
        log.info("[CLIENT] profile update dto={}", LogSanitizer.brief(updatedClient));
    }
}
