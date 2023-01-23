package com.movies.movies;

public class MovieNotFoundException extends RuntimeException {
    public MovieNotFoundException(Long id) {
        super("Could not find Movie " + id);

    }
}
