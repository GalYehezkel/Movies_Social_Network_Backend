package com.movies.movies;

public class LoginException extends RuntimeException {
    public LoginException() {
        super("User name or password are incorrect ");

    }
}
