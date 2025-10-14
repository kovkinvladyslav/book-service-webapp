package com.epam.rd.autocode.spring.project.exception;

public class UserAlreadyExists extends AlreadyExistException {
    public UserAlreadyExists(String message) {
        super(message);
    }
}
