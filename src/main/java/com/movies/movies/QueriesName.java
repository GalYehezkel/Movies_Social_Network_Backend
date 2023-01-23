package com.movies.movies;

public class QueriesName {
    // One parameter
    public final static String similarByLanguage = "select m1.movie_id, m1.title, m1.language, m1.budget, m1.revenue, m1.runtime, m1.production_company " +
            "from movies_social_network.movies AS m1, movies_social_network.movies AS m2 " +
            "WHERE m2.movie_id = ? && m1.language = m2.language;";
    public final static String similarByCompany = "select m1.movie_id, m1.title, m1.language, m1.budget, m1.revenue, m1.runtime, m1.production_company " +
            "from movies_social_network.movies AS m1, movies_social_network.movies AS m2 " +
            "WHERE m2.movie_id = ? && m1.production_company = m2.production_company;";
    public final static String byUserName = "SELECT m.movie_id, m.title, m.language, m.budget, m.revenue, m.runtime, m.production_company " +
            "FROM movies_social_network.movies AS m JOIN movies_social_network.likes AS l " +
            "JOIN movies_social_network.users AS u " +
            "WHERE u.user_name = ? " +
            "&& l.user_id = u.user_id " +
            "&& m.movie_id = l.movie_id;";
    public final static String allMoviesFromSameGenreLikeMovieId = "select m.movie_id, m.title, m.language, m.budget, m.revenue, m.runtime, m.production_company " +
            "from movies_social_network.movies AS m JOIN movies_social_network.movies_genres AS mg " +
            "JOIN movies_social_network.genres AS g " +
            "WHERE g.genre_id = mg.genre_id && " +
            "m.movie_id = mg.movie_id && " +
            "g.genre_id IN ( " +
            "SELECT genre_id " +
            "FROM movies_social_network. movies_genres " +
            "WHERE movie_id = ? " +
            ") " +
            "GROUP BY m.movie_id;";
    public final static String moviesUserXLikedMostByCompany = "SELECT movie_id, title, language, budget, revenue, runtime, production_company  " +
            "FROM movies  " +
            "WHERE production_company = (  " +
            "SELECT m.production_company  " +
            "FROM movies_social_network.movies AS m JOIN movies_social_network.likes AS l  " +
            "JOIN movies_social_network.users AS u  " +
            "WHERE u.user_name = ?  " +
            "&& l.user_id = u.user_id  " +
            "&& m.movie_id = l.movie_id  " +
            "&& m.production_company <> \"[]\"  " +
            "GROUP BY production_company  " +
            "ORDER BY COUNT(m.movie_id) DESC  " +
            "LIMIT 1)  ";

    public final static String moviesUserXLikedMostByGenre = "SELECT movies.movie_id, title, language, budget, revenue, runtime, production_company " +
            "FROM movies, movies_genres " +
            "WHERE movies.movie_id = movies_genres.movie_id  " +
            "&& movies_genres.genre_id = ( " +
            "select s.genre_id " +
            "from(( " +
            "SELECT m.movie_id " +
            "FROM movies_social_network.movies AS m  " +
            "JOIN movies_social_network.likes AS l " +
            "JOIN movies_social_network.users AS u " +
            "WHERE u.user_name = ? " +
            "&& l.user_id = u.user_id " +
            "&& m.movie_id = l.movie_id) AS f  " +
            "JOIN movies_genres as s) " +
            "WHERE f.movie_id = s.movie_id " +
            "GROUP BY genre_id " +
            "ORDER BY COUNT(s.movie_id) DESC " +
            "LIMIT 1) ";

    public final static String moviesUserXLikedByRuntimeAverage = "SELECT movie_id, title, language, budget, revenue, runtime, production_company " +
            "FROM movies AS m " +
            "WHERE runtime > (SELECT AVG(m.runtime) " +
            "FROM movies_social_network.movies AS m  " +
            "JOIN movies_social_network.likes AS l " +
            "JOIN movies_social_network.users AS u " +
            "WHERE u.user_name = ? " +
            "&& l.user_id = u.user_id " +
            "&& m.movie_id = l.movie_id " +
            "&& m.runtime <> 0) - 1 " +
            "&& runtime < (SELECT AVG(m.runtime) " +
            "FROM movies_social_network.movies AS m  " +
            "JOIN movies_social_network.likes AS l " +
            "JOIN movies_social_network.users AS u " +
            "WHERE u.user_name = ? " +
            "&& l.user_id = u.user_id " +
            "&& m.movie_id = l.movie_id " +
            "&& m.runtime <> 0) + 1 ";

    public final static String runTimeBiggerThan = "SELECT movies.movie_id, movies.title, movies.language, movies.budget, movies.revenue, movies.runtime, movies.production_company " +
            "from movies  " +
            "where movies.runtime > ? ";
    public final static String runTimeSmallerThan = "SELECT movies.movie_id, movies.title, movies.language, movies.budget, movies.revenue, movies.runtime, movies.production_company " +
            "from movies  " +
            "where movies.runtime < ? ";
    // LIKE
    public final static String byLanguage = "SELECT * " +
            "FROM movies_social_network.movies " +
            "WHERE movies.language LIKE ?;";
    public final static String byCompany = "SELECT * " +
            "FROM movies_social_network.movies " +
            "WHERE movies.production_company LIKE ?;";
    public final static String movieNameContainsOrKeyword = "SELECT * FROM movies " +
            "WHERE title LIKE ? " +
            "union " +
            "SELECT movies.movie_id, movies.title, movies.language, movies.budget, movies.revenue, movies.runtime, movies.production_company " +
            "from movies JOIN keyword_movie JOIN keywords " +
            "where movies.movie_id = keyword_movie.movie_id " +
            "&& keywords.keyword_id = keyword_movie.keyword_id " +
            "&& keywords.name LIKE ? ";
    public final static String byGenre = "select m.movie_id, m.title, m.language, m.budget, m.revenue, m.runtime, m.production_company " +
            "from movies_social_network.movies AS m JOIN movies_social_network.movies_genres  AS mg " +
            "JOIN movies_social_network.genres AS g " +
            "WHERE g.genre_id = mg.genre_id && " +
            "m.movie_id = mg.movie_id && " +
            "g.name LIKE ?;";

    // No parameter
    public final static String movieBasicQuery = "SELECT * " +
            "FROM movies_social_network.movies AS m1 " +
            "WHERE runtime > 0 && " +
            "revenue > 100 && " +
            "budget > 100 limit 100";
    public final static String mostProfitForEveryCompany = "SELECT  m2.movie_id, m2.title, m2.language, m2.budget, m2.revenue, m2.runtime, m2.production_company " +
            "from " +
            "(SELECT m.production_company,  MAX(m.revenue) AS mmm " +
            "FROM movies_social_network.movies AS m " +
            "GROUP BY m.production_company) AS m1 JOIN movies_social_network.movies AS m2 " +
            "WHERE m2.production_company = m1.production_company && " +
            " m1.mmm = m2.revenue " +
            " GROUP BY m2.production_company " +
            "ORDER BY m2.runtime DESC;";
    public final static String mostProfitForEveryLanguage = "SELECT  m2.movie_id, m2.title, m2.language, m2.budget, m2.revenue, m2.runtime, m2.production_company " +
            "from " +
            "(SELECT language, MAX(revenue) AS mmm " +
            "FROM movies_social_network.movies " +
            "GROUP BY language ) AS m1 JOIN movies_social_network.movies AS m2 " +
            "WHERE m2.language = m1.language && " +
            " m1.mmm = m2.revenue " +
            " GROUP BY m2.language " +
            " ORDER BY m2.revenue DESC;";
    public final static String allMoviesOrderedByLikes = "select s.movie_id, s.title, s.language, s.budget, s.revenue, s.runtime, s.production_company " +
            "from ( " +
            "             SELECT COUNT(m.movie_id) as ll, m.movie_id, m.title, m.language, m.budget, m.revenue, m.runtime, m.production_company " +
            "              FROM (likes as l join movies as m) " +
            "              WHERE l.movie_id = m.movie_id               " +
            "              GROUP BY m.movie_id  " +
            "              ORDER BY ll DESC)as s  " +
            " " +
            " UNION " +
            "     " +
            " select d.movie_id, d.title, d.language, d.budget, d.revenue, d.runtime, d.production_company " +
            "from movies as d " +
            "where d.movie_id not in ( " +
            "select likes.movie_id " +
            "from likes) ";
    public final static String allMoviesOrderedByNameLength = "SELECT *  " +
            "FROM movies  " +
            "ORDER BY CHAR_LENGTH(title) DESC ";
}
