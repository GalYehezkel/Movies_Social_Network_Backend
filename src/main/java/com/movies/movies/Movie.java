package com.movies.movies;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Movie {
    private Integer movieId;
    private String title;
    private String language;
    private Integer budget;
    private Double revenue;
    private Integer runtime;
    private String production_company;
    private Integer number_of_likes;
    private Boolean user_like;
    private List<String> genres;
}
