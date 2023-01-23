package com.movies.movies;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class FailAdvice {
    @ResponseBody
    @ExceptionHandler(LoginException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String loginFailHandler(LoginException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(CreateUserException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String createUserFailHandler(CreateUserException ex) {
        return ex.getMessage();
    }
}
