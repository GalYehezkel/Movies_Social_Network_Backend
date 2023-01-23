package com.movies.movies;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
public class MoviesController {
    private  MoviesFunctions moviesFunctions = new MoviesFunctions();

    @PostMapping("/login")
    @CrossOrigin(origins = "http://localhost:3000")
    UserId login(@RequestBody Login login) {
        Integer userId = moviesFunctions.login(login);
        if (userId != null) {
            return new UserId(userId, login.getUser_name());
        } else {
            throw new LoginException();
        }
    }

    @PutMapping("/create-user")
    @CrossOrigin(origins = "http://localhost:3000")
    UserId createUser(@RequestBody CreateUser createUser) {
        Integer userId = moviesFunctions.createUser(createUser);
        if (userId != null) {
            return new UserId(userId, createUser.getUser_name());
        } else {
            throw new CreateUserException();
        }
    }

    @PutMapping("/change-like")
    @CrossOrigin(origins = "http://localhost:3000")
    void changeLike(@RequestParam(name = "user_id") Integer userId, @RequestParam(name = "movie_id") Integer movieId) {
        moviesFunctions.changeLike(userId, movieId);
    }

    @GetMapping("/users")
    @CrossOrigin(origins = "http://localhost:3000")
    List<UserId> getUsers() {
        return moviesFunctions.getUsers();
    }

    @GetMapping("/movies/query")
    @CrossOrigin(origins = "http://localhost:3000")
    List<Movie> getMoviesWithInput(@RequestParam(name = "query_name") String queryName, @RequestParam(name = "user_id") Integer userId, @RequestParam(name = "input") String input) {
        return moviesFunctions.getMoviesByQueryName(queryName,userId, input);
    }

    @GetMapping("/movies/query-like")
    @CrossOrigin(origins = "http://localhost:3000")
    List<Movie> getMoviesByOperatorLike(@RequestParam(name = "query_name") String queryName, @RequestParam(name = "user_id") Integer userId, @RequestParam(name = "input") String input) {
        return moviesFunctions.getMoviesByQueryNameLike(queryName,userId, input);
    }

    @GetMapping("/movies/query-no-input")
    @CrossOrigin(origins = "http://localhost:3000")
    List<Movie> getMoviesNoInput(@RequestParam(name = "query_name") String queryName, @RequestParam(name = "user_id") Integer userId) {
        return moviesFunctions.getMoviesByQueryNameNoInput(queryName,userId);
    }

    @GetMapping("/movies/query-three-input")
    @CrossOrigin(origins = "http://localhost:3000")
    List<Movie> getMoviesThreeInputs(@RequestParam(name = "user_id") Integer userId, @RequestParam(name = "input_one") String inputOne,
                                     @RequestParam(name = "input_two") String inputTwo,
                                     @RequestParam(name = "input_three") String inputThree) {
        return moviesFunctions.getMoviesByQueryNameThreeParams(userId, inputOne, inputTwo, inputThree);
    }
}