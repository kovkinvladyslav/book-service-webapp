package com.epam.rd.autocode.spring.project.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AlreadyExistException.class)
    public String handleAlreadyExists(AlreadyExistException alreadyExistException, Model model) {
        model.addAttribute("errorMessage", alreadyExistException.getMessage());
        return "redirect:/error/already-exists.html";
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException notFoundException, Model model) {
        model.addAttribute("errorMessage", notFoundException.getMessage());
        return "redirect:/error/404.html";
    }

    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public String forbiddenPage(){
        return "redirect:/";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "redirect:/error/insufficient-funds.html";
    }

    @ExceptionHandler(UserAlreadyExists.class)
    public String handleRegistrationError(AlreadyExistException ex) {
        return "redirect:/register?error=exists";
    }
}