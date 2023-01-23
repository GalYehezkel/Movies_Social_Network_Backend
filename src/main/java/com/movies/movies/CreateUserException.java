package com.movies.movies;

public class CreateUserException extends RuntimeException {
    public CreateUserException() {
        super("User creation failed, user name already exists");

    }
}
