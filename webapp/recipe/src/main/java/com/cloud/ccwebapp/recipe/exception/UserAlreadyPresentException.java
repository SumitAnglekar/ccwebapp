package com.cloud.ccwebapp.recipe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UserAlreadyPresentException extends RuntimeException {
    public UserAlreadyPresentException(String message) {
        super(message);
        System.out.println("Here");
    }
}