package com.movies.movies;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.*;

/**
 * Different types of JDBC usage
 */
public class MoviesFunctions {
	private Connection conn; // DB connection
	private Map<Integer, Integer> movieToLikesMap;
	private Map<String, String> queryNameToQueryMap;
	private Map<Integer, List<String>> movieToGenresMap;
	private ConnectionToDB connectionToDB;
	private int limitSize = 1000;

	/**
	 * Empty constructor
	 * open connection to DB, init maps.
	 */
	public MoviesFunctions() {
		this.conn = null;
		try {
			connectionToDB = new ConnectionToDB();
		} catch (Exception e) {
			System.out.println("Error connecting to DB : " + e.getMessage());
		}
		conn = connectionToDB.openConnection();
		movieToLikesMap = new HashMap<>();
		queryNameToQueryMap = new HashMap<>();
		movieToGenresMap = new HashMap<>();
		initLikesMap();
		initQueryNamesMap();
		initGenresMap();
	}

	/**
	 * init map with key of movie id to value number of likes
	 */
	private void initLikesMap() {
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select movie_id, COUNT(user_id) as number_of_likes from likes group by movie_id");) {

			while (rs.next() == true) {
				Integer movieId = rs.getInt("movie_id");
				Integer numberOfLikes = rs.getInt("number_of_likes");
				movieToLikesMap.put(movieId, numberOfLikes);
			}
		} catch (SQLException e) {
			System.out.println("ERROR executeQuery - " + e.getMessage());
		}
	}

	/**
	 * init map with key of query name to value sql query
	 */
	private void initQueryNamesMap() {
		queryNameToQueryMap.put("movieBasicQuery", QueriesName.movieBasicQuery);
		queryNameToQueryMap.put("similarByCompany", QueriesName.similarByCompany);
		queryNameToQueryMap.put("similarByLanguage", QueriesName.similarByLanguage);
		queryNameToQueryMap.put("byLanguage", QueriesName.byLanguage);
		queryNameToQueryMap.put("byCompany", QueriesName.byCompany);
		queryNameToQueryMap.put("movieNameContainsOrKeyword", QueriesName.movieNameContainsOrKeyword);
		queryNameToQueryMap.put("byGenre", QueriesName.byGenre);
		queryNameToQueryMap.put("byUserName", QueriesName.byUserName);
		queryNameToQueryMap.put("mostProfitForEveryCompany", QueriesName.mostProfitForEveryCompany);
		queryNameToQueryMap.put("mostProfitForEveryLanguage", QueriesName.mostProfitForEveryLanguage);
		queryNameToQueryMap.put("allMoviesFromSameGenreLikeMovieId", QueriesName.allMoviesFromSameGenreLikeMovieId);
		queryNameToQueryMap.put("allMoviesOrderedByLikes", QueriesName.allMoviesOrderedByLikes);
		queryNameToQueryMap.put("moviesUserXLikedMostByCompany", QueriesName.moviesUserXLikedMostByCompany);
		queryNameToQueryMap.put("moviesUserXLikedMostByGenre", QueriesName.moviesUserXLikedMostByGenre);
		queryNameToQueryMap.put("moviesUserXLikedByRuntimeAverage", QueriesName.moviesUserXLikedByRuntimeAverage);
		queryNameToQueryMap.put("allMoviesOrderedByNameLength", QueriesName.allMoviesOrderedByNameLength);
		queryNameToQueryMap.put("runTimeSmallerThan", QueriesName.runTimeSmallerThan);
		queryNameToQueryMap.put("runTimeBiggerThan", QueriesName.runTimeBiggerThan);
	}

	/**
	 * init map with key of movie id to genres list
	 */
	private void initGenresMap() {
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select mg.movie_id, g.name " +
				"from movies_genres as mg left join genres as g " +
				"on mg.genre_id = g.genre_id;");) {
			Integer currentMovieId = 2;
			List<String> genresList = new LinkedList<>();
			while (rs.next() == true) {
				Integer movieId = rs.getInt("movie_id");
				if(!movieId.equals(currentMovieId)) {
					movieToGenresMap.put(currentMovieId, genresList);
					currentMovieId = movieId;
					genresList = new LinkedList<>();
				}
				String genreName = rs.getString("name");
				genresList.add(genreName);

			}
		} catch (SQLException e) {
			System.out.println("ERROR executeQuery - " + e.getMessage());
		}
	}

	/**
	 * get movies by query name, query has one input parameter
	 */
	public List<Movie> getMoviesByQueryName(String queryName, Integer userId, String input) {
		List<Movie> movieList = new LinkedList<>();
		int counter = 0;
		try (PreparedStatement pstmt = conn.prepareStatement(queryNameToQueryMap.get(queryName))) {
			pstmt.setString(1, input);
			if(queryName.equals("moviesUserXLikedByRuntimeAverage")) {
				pstmt.setString(2, input);
			}
			ResultSet rs = pstmt.executeQuery();
			while (rs.next() == true && counter !=  limitSize) {
				Movie movie = new Movie();
				Integer movieId = rs.getInt("movie_id");
				movie.setMovieId(movieId);
				movie.setTitle((rs.getString("title")));
				movie.setLanguage((rs.getString("language")));
				movie.setBudget((rs.getInt("budget")));
				movie.setRevenue((rs.getDouble("revenue")));
				movie.setProduction_company((rs.getString("production_company")));
				movie.setRuntime((rs.getInt("runtime")));
				movie.setNumber_of_likes(movieToLikesMap.get(movieId) != null ? movieToLikesMap.get(movieId) : 0);
				movie.setGenres(movieToGenresMap.get(movieId));
				String query = "SELECT * FROM movies as m join (select * from likes where user_id=?) as l where m.movie_id = l.movie_id and m.movie_id = ?";

				try (PreparedStatement pstmt2 = conn.prepareStatement(query)) {
					pstmt2.setInt(1, userId);
					pstmt2.setInt(2, movieId);
					ResultSet rs2 = pstmt2.executeQuery();
					if (rs2.next() == false) {
						movie.setUser_like(false);
					} else {
						movie.setUser_like(true);
					}
				}
				movieList.add(movie);
				counter++;
			}
		} catch (SQLException e) {
			System.out.println("ERROR executeQuery - " + e.getMessage());
		}
		return movieList;
	}

	/**
	 * get movies by query name, query has one input parameter, query has Like in structure
	 */
	public List<Movie> getMoviesByQueryNameLike(String queryName, Integer userId, String input) {
		List<Movie> movieList = new LinkedList<>();
		int counter = 0;
		try (PreparedStatement pstmt = conn.prepareStatement(queryNameToQueryMap.get(queryName))) {
			pstmt.setString(1, "%" + input + "%");
			if(queryName.equals("movieNameContainsOrKeyword")) {
				pstmt.setString(2, "%" + input + "%");
			}
			ResultSet rs = pstmt.executeQuery();
			while (rs.next() == true && counter !=  limitSize) {
				Movie movie = new Movie();
				Integer movieId = rs.getInt("movie_id");
				movie.setMovieId(movieId);
				movie.setTitle((rs.getString("title")));
				movie.setLanguage((rs.getString("language")));
				movie.setBudget((rs.getInt("budget")));
				movie.setRevenue((rs.getDouble("revenue")));
				movie.setProduction_company((rs.getString("production_company")));
				movie.setRuntime((rs.getInt("runtime")));
				movie.setNumber_of_likes(movieToLikesMap.get(movieId) != null ? movieToLikesMap.get(movieId) : 0);
				movie.setGenres(movieToGenresMap.get(movieId));
				String query = "SELECT * FROM movies as m join (select * from likes where user_id=?) as l where m.movie_id = l.movie_id and m.movie_id = ?";

				try (PreparedStatement pstmt2 = conn.prepareStatement(query)) {
					pstmt2.setInt(1, userId);
					pstmt2.setInt(2, movieId);
					ResultSet rs2 = pstmt2.executeQuery();
					if (rs2.next() == false) {
						movie.setUser_like(false);
					} else {
						movie.setUser_like(true);
					}
				}
				movieList.add(movie);
				counter++;
			}
		} catch (SQLException e) {
			System.out.println("ERROR executeQuery - " + e.getMessage());
		}
		return movieList;
	}

	/**
	 * get movies by query name, query has no input parameter
	 */
	public List<Movie> getMoviesByQueryNameNoInput(String queryName, Integer userId) {
		List<Movie> movieList = new LinkedList<>();
		int counter = 0;
		try (PreparedStatement pstmt = conn.prepareStatement(queryNameToQueryMap.get(queryName))) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next() == true && counter !=  limitSize) {
				Movie movie = new Movie();
				Integer movieId = rs.getInt("movie_id");
				movie.setMovieId(movieId);
				movie.setTitle((rs.getString("title")));
				movie.setLanguage((rs.getString("language")));
				movie.setBudget((rs.getInt("budget")));
				movie.setRevenue((rs.getDouble("revenue")));
				movie.setProduction_company((rs.getString("production_company")));
				movie.setRuntime((rs.getInt("runtime")));
				movie.setNumber_of_likes(movieToLikesMap.get(movieId));
				movie.setGenres(movieToGenresMap.get(movieId));
				String query = "SELECT * FROM movies as m join (select * from likes where user_id=?) as l where m.movie_id = l.movie_id and m.movie_id = ?";

				try (PreparedStatement pstmt2 = conn.prepareStatement(query)) {
					pstmt2.setInt(1, userId);
					pstmt2.setInt(2, movieId);
					ResultSet rs2 = pstmt2.executeQuery();
					if (rs2.next() == false) {
						movie.setUser_like(false);
					} else {
						movie.setUser_like(true);
					}
				}
				movieList.add(movie);
				counter++;
			}
		} catch (SQLException e) {
			System.out.println("ERROR executeQuery - " + e.getMessage());
		}
		return movieList;
	}

	/**
	 * get movies by query name, query has one input parameter
	 */
	public List<Movie> getMoviesByQueryNameThreeParams(Integer userId, String inputOne, String inputTwo, String inputThree) {
		List<Movie> movieList = new LinkedList<>();
		int counter = 0;
		try (PreparedStatement pstmt = conn.prepareStatement(makeQueryFromThreeParams(inputOne, inputTwo, inputThree))) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next() == true && counter !=  limitSize) {
				Movie movie = new Movie();
				Integer movieId = rs.getInt("movie_id");
				movie.setMovieId(movieId);
				movie.setTitle((rs.getString("title")));
				movie.setLanguage((rs.getString("language")));
				movie.setBudget((rs.getInt("budget")));
				movie.setRevenue((rs.getDouble("revenue")));
				movie.setProduction_company((rs.getString("production_company")));
				movie.setRuntime((rs.getInt("runtime")));
				movie.setNumber_of_likes(movieToLikesMap.get(movieId) != null ? movieToLikesMap.get(movieId) : 0);
				movie.setGenres(movieToGenresMap.get(movieId));
				String query = "SELECT * FROM movies as m join (select * from likes where user_id=?) as l where m.movie_id = l.movie_id and m.movie_id = ?";

				try (PreparedStatement pstmt2 = conn.prepareStatement(query)) {
					pstmt2.setInt(1, userId);
					pstmt2.setInt(2, movieId);
					ResultSet rs2 = pstmt2.executeQuery();
					if (rs2.next() == false) {
						movie.setUser_like(false);
					} else {
						movie.setUser_like(true);
					}
				}
				movieList.add(movie);
				counter++;
			}
		} catch (SQLException e) {
			System.out.println("ERROR executeQuery - " + e.getMessage());
		}
		return movieList;
	}

	/**
	 * login with username and password, returns user id. Has error handling.
	 */
	public UserId login(Login login) {
		String query = "select * from users where user_name = ? and password = ?;";

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, login.getUser_name());
			pstmt.setString(2, login.getPassword());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next() == true) {
				UserId userId = new UserId();
				try (PreparedStatement pstmt2 = conn.prepareStatement(QueriesName.userLikesAmount)) {
					pstmt2.setString(1, login.getUser_name());
					ResultSet rs2 = pstmt2.executeQuery();
					if (rs2.next() == false) {
						userId.setUserLikes(0);
					} else {
						userId.setUserLikes(rs2.getInt("likes"));
					}
				}
				userId.setUserId(rs.getInt("user_id"));
				userId.setUserName(rs.getString("user_name"));
				return userId;
			}
		}
		catch (SQLException e) {
			System.out.println("ERROR executeQuery - " + e.getMessage());
		}
		return null;
	}

	public UserId createUser(CreateUser createUser) {
		String query = "INSERT INTO users(user_name, password) VALUES(?,?)";
		try (PreparedStatement pstmt = conn.prepareStatement(query);) {
			pstmt.setString(1, createUser.getUser_name());
			pstmt.setString(2, createUser.getPassword());
			pstmt.executeUpdate();
			return login(new Login(createUser.getUser_name(), createUser.getPassword()));
		} catch (SQLException e) {
			System.out.println("ERROR creating user - " + e.getMessage());
		}
		return null;
	}

	// *** This is without validations of userId and movieId ***
	/**
	 * change like of user to specific movie id.
	 */
	public void changeLike(Integer userId, Integer movieId) {
		String query = "select * from likes where user_id = ? and movie_id = ?;";

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, movieId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next() == true) {
				String queryDelete = "delete from likes where user_id = ? and movie_id = ?";
				PreparedStatement pstmtDelete = conn.prepareStatement(queryDelete);
				pstmtDelete.setInt(1, userId);
				pstmtDelete.setInt(2, movieId);
				pstmtDelete.executeUpdate();
				Integer numberOfLikes = movieToLikesMap.get(movieId) == null ? movieToLikesMap.put(movieId, 0) : movieToLikesMap.put(movieId, movieToLikesMap.get(movieId) - 1);
			} else {
				String queryInsert = "INSERT INTO likes(user_id, movie_id) VALUES(?,?)";
				PreparedStatement pstmtInsert = conn.prepareStatement(queryInsert);
				pstmtInsert.setInt(1, userId);
				pstmtInsert.setInt(2, movieId);
				pstmtInsert.executeUpdate();
				Integer numberOfLikes = movieToLikesMap.get(movieId) == null ? movieToLikesMap.put(movieId, 1) : movieToLikesMap.put(movieId, movieToLikesMap.get(movieId) + 1);
			}
		}
		catch (SQLException e) {
			System.out.println("ERROR executeQuery - " + e.getMessage());
		}
	}

	/**
	 * insert users to DB
	 */
	public void insertUsers(){
		int result;
		String query = "INSERT INTO users(user_name, password) VALUES(?,?)";
		try (PreparedStatement pstmt = conn.prepareStatement(query);) {

			BufferedReader reader = new BufferedReader(new FileReader("Backend/src/users.csv"));


			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] s = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

				pstmt.setString(1, s[1]);
				pstmt.setString(2, s[2]);
				pstmt.executeUpdate();
			}



			System.out.println("Success - demoWithPreparedStatement");

		} catch (SQLException e) {
			System.out.println("ERROR demoWithPreparedStatement - " + e.getMessage());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * creates movie id list from csv
	 */
	public List<String> moviesIdList() throws IOException {

		List<String> idMap = new ArrayList<>();

		BufferedReader readerMovieId = new BufferedReader(new FileReader("Backend/src/moviesId.csv"));

		String lineMovie = null;

		while ((lineMovie = readerMovieId.readLine()) != null) {
			idMap.add(lineMovie);

		}

		return idMap;

	}

	/**
	 * insert likes to DB
	 */
	public void insertLikes() throws IOException {

		List<String> movies = moviesIdList();
		List<String[]> userLikesId = new ArrayList<String[]>();
		Map<String[], Integer> map = new HashMap<>();



		String query = "INSERT INTO likes(user_id, movie_id) VALUES(?,?)";


		try (PreparedStatement pstmt = conn.prepareStatement(query);) {

			BufferedReader reader = new BufferedReader(new FileReader("Backend/src/likes.csv"));
			BufferedReader readerMovieId = new BufferedReader(new FileReader("Backend/src/moviesId.csv"));

			int j=0;
			String line = null;
			String[] s = new String[2];
			String[] pairs = new String[2];
			int movieId = 0;
			reader.readLine(); // read the labels
			while ((line = reader.readLine()) != null ) {
				s = line.split(",");

				if(Objects.equals(s[0], "92")){
					break;
				}
				movieId = Integer.parseInt(s[1]);

				while (!movies.contains(Integer.toString(movieId))){
					movieId++;

				}
				//userLikesId.put(s[0],Integer.toString(movieId));
				pairs[0] = s[0];
				pairs[1] = Integer.toString(movieId) ;

				userLikesId.add(pairs);

				pairs = new String[2];

				j++;
				System.out.println("user: "+ s[0]);
				System.out.println("movie: "+ Integer.toString(movieId));
				System.out.println("j: "+ j);
			}

			int k = 0;
			boolean exist = false;
			for(String[] p: userLikesId){
				exist = false;
				for(Map.Entry<String[], Integer> e : map.entrySet()){
					if (Arrays.equals(p, e.getKey())) {
						exist = true;

						break;
					}
				}
				if(!exist){
					map.put(p, k);
					k++;
				}

				System.out.println("map k: "+ k);
			}

			k = 0;
			for(Map.Entry<String[], Integer> e : map.entrySet()){
				pstmt.setString(1, e.getKey()[0]);
				pstmt.setString(2, e.getKey()[1]);
				pstmt.executeUpdate();
				k++;
				System.out.println("k: "+ k);

			}



			System.out.println("Success - demoWithPreparedStatement");

		} catch (SQLException e) {
			System.out.println("ERROR demoWithPreparedStatement - " + e.getMessage());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * insert genres to DB
	 */
	public void insertGenres() {
		int result;
		Map<String, String> map = new TreeMap<>();
		String query = "INSERT INTO genres(genre_id, name) VALUES(?,?)";

		try (PreparedStatement pstmt = conn.prepareStatement(query);) {

			BufferedReader reader = new BufferedReader(new FileReader("Backend/src/genresJson.csv"));


			String line = null;
			String[] s = new String[1];
			reader.readLine(); // read the labels
			int j=0;
			while ((line = reader.readLine()) != null) {

				s = line.split(",");
				if (s[1].equals("[]")) {
					continue;
				}
				for(int i = 1; i < s.length; i+=2) {
					String id = s[i].split(":")[1].split(" ")[1];
					String name = s[i + 1].split(":")[1].split("}")[0].replace('"', ' ').replace(" ", "");
					map.put(id, name);
//					pstmt.setString(1, id);
//					pstmt.setString(2, name);

				}
				j++;
				System.out.println("j: "+ j);


			}
			int k = 0;
			for(Map.Entry<String, String> e : map.entrySet()){
				pstmt.setString(1, e.getKey());
				pstmt.setString(2, e.getValue());
				pstmt.executeUpdate();
				k++;
				System.out.println("k: "+ k);

			}



			System.out.println("Success - demoWithPreparedStatement");

		} catch (SQLException e) {
			System.out.println("ERROR demoWithPreparedStatement - " + e.getMessage());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * insert genre-movie to DB
	 */
	public void insertGenreMovie() throws IOException {
		int result;
		String query = "INSERT INTO movies_genres(movie_id, genre_id) VALUES(?,?)";

		try (PreparedStatement pstmt = conn.prepareStatement(query);) {

			BufferedReader readerGenre = new BufferedReader(new FileReader("Backend/src/genresJson.csv"));
			BufferedReader readerMovie = new BufferedReader(new FileReader("Backend/src/moviesId.csv"));

			String line = null;
			String lineMovie = null;
			String[] s = new String[1];
			String[] movies = new String[1];
			int j=0;
			while ((line = readerGenre.readLine()) != null && (lineMovie = readerMovie.readLine()) != null) {
				if (line.equals("[]")) {
					continue;
				}
				s = line.split(",");
				for(int i = 0; i < s.length; i+=2) {
					String id = s[i].split(":")[1].split(" ")[1];
					System.out.println("movie id: "+ lineMovie + " genre: "+id);
					pstmt.setString(1, lineMovie);
					pstmt.setString(2, id);
					pstmt.executeUpdate();
				}
				j++;
				System.out.println("j:"+ j);
			}



			System.out.println("Success - demoWithPreparedStatement");

		} catch (SQLException e) {
			System.out.println("ERROR demoWithPreparedStatement - " + e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * insert keywords to DB
	 */
	public void insertKeywords() throws IOException {

		Map<String, String> map = new TreeMap<>();
		String query = "INSERT INTO keywords(keyword_id, name) VALUES(?,?)";

		try (PreparedStatement pstmt = conn.prepareStatement(query);) {

			BufferedReader reader = new BufferedReader(new FileReader("src/main/java/com/movies/movies/keywords.csv"));

			String line = null;
			String[] s = new String[1];
			int j = 0;
			while ((line = reader.readLine()) != null) {
				if (line.equals("[]")) {
					continue;
				}
				s = line.split(",");
				stringValidation(s);
				j++;
				for(int i = 0; i < s.length; i+=2) {

					if(s[i].equals("null") && i == s.length -1){
						continue;
					}
					else if(s[i].equals("null")){
						i++;
					}
					String id = s[i].split(":")[1].split(" ")[1];
					String name = s[i + 1].split(":")[1].split("}")[0].replace('"', ' ').replace(" ", "");
					name = name.split("'")[1];
					map.put(id, name);
					System.out.println("j:"+ j);
					System.out.println("name: "+ name);
					System.out.println("name: "+ id);
//					pstmt.setString(1, id);
//					pstmt.setString(2, name);
//					pstmt.executeUpdate();
				}
			}
			int k = 0;

			for(Map.Entry<String, String> e : map.entrySet()){
				pstmt.setString(1, e.getKey());
				pstmt.setString(2, e.getValue());
				pstmt.executeUpdate();
				k++;
				System.out.println("k: "+ k);

			}




			System.out.println("Success - insertKeywords");

		} catch (Exception e) {
			System.out.println("ERROR insertKeywords - " + e.getMessage());
		}
	}

	/**
	 * insert keyword-movie to DB
	 */
	public void insertKeywordMovie() throws IOException {
		int j = 0;
		String query = "INSERT INTO keyword_movie(movie_id, keyword_id) VALUES(?,?)";

		try (PreparedStatement pstmt = conn.prepareStatement(query);) {

			BufferedReader readerGenre = new BufferedReader(new FileReader("src/main/java/com/movies/movies/keywords.csv"));
			BufferedReader readerMovie = new BufferedReader(new FileReader("src/main/java/com/movies/movies/moviesId.csv"));

			String line = null;
			String lineMovie = null;
			String[] s = new String[1];
			String[] movies = new String[1];
			while ((line = readerGenre.readLine()) != null && (lineMovie = readerMovie.readLine()) != null) {
				j++;
				if (line.equals("[]")) {
					continue;
				}
				s = line.split(",");
				stringValidation(s);
				for(int i = 0; i < s.length; i+=2) {

					if(s[i].equals("null") && i == s.length -1){
						continue;
					}
					else if(s[i].equals("null")){
						i++;
					}

					String id = s[i].split(":")[1].split(" ")[1];
					System.out.println("j:"+ j);
					System.out.println("lineMovie:"+ lineMovie);
					System.out.println("ud:"+ id);
					pstmt.setString(1, lineMovie);
					pstmt.setString(2, id);
					pstmt.executeUpdate();
				}
			}



			System.out.println("Success - demoWithPreparedStatement");

		} catch (SQLException e) {
			System.out.println("ERROR demoWithPreparedStatement - " + e.getMessage());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * insert movies to DB
	 */
	public void insertMovies(){
		int result;
		String query = "INSERT INTO movies(movies_id, title, language, budget, revenue, runtime, production_company ) VALUES(?,?,?,?,?,?,?)";

		try (PreparedStatement pstmt = conn.prepareStatement(query);) {

			BufferedReader reader = new BufferedReader(new FileReader("Backend/src/movies_metadata.csv"));
			//CSVReader reader = new CSVReader(new FileReader("Backend/src/movies_metadata.csv"));

			/**
			 *	1. movies_id
			 * 	2. title
			 * 	3. language
			 * 	4. budget
			 * 	5. revenue
			 * 	6. runtime
			 * 	7. production_companies
			 */
			/*duplicate primary.key: Bubble, the promise, Blackout 141971, A Farewell to Arms 22649 Offside 13209
			 * Blackout 141971 Black Gold 77221 Rich and Famous 109962 A Place at the Table
			 * * no title: line: 19,721 (under Diplomatic Siege), 29482, 35558
			 * The Tempest 119916 The Congress 152795
			 *  Days of Darkness 18440 Brotherhood25541  Camille Claudel 1915110428 Deal 11115 The Phantom of the Opera
			 * Nana, the True Key of Pleasure King Lear Wife 132641 Clockstoppers Confessions of a Dangerous Mind
			 * Why We Fight: Divide and Conquer, The Viking 99080, Cemetery of Splendour, Seven Years Bad Luck,
			 * Pokémon: Spell of the Unknown,  Force Majeure
			 * */

			String line = null;
			int i = 0;
			reader.readLine(); // read the labels
			while ((line = reader.readLine()) != null) {

				String[] s = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				System.out.println("movie title: "+ s[5]);
				pstmt.setString(1, s[1]); //id
				pstmt.setString(2, s[5]); //title
				pstmt.setString(3, s[2]); //language
				pstmt.setString(4, s[0]); //budget
				pstmt.setString(5, s[3]); //revenue
				pstmt.setString(6, s[4]); //runtime

				//production_companies
				String production = s[6];
				if (!Objects.equals(s[6], "[]")){
					String[] splitProd = production.split(":")[1].split(",");
					pstmt.setString(7, splitProd[0].split("'")[1]); //runtime
				} else{
					pstmt.setString(7, s[6]); //runtime
				}


				pstmt.executeUpdate();
				i++;
				System.out.println("i: "+ i);

			}



			System.out.println("Success - demoWithPreparedStatement");

		} catch (SQLException e) {
			System.out.println("ERROR demoWithPreparedStatement - " + e.getMessage());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void stringToNull(String[] strings, int i){

		strings[i - 1] = strings[i - 1] + strings[i];
		strings[i] = "null";

	}
	public void stringValidation(String[] strings){

		int i = 0;
		int j = strings.length;

		for(i=0; i < j; i++){

			if( i > 1 && i < j-1){
				if( !strings[i-1].contains("id") && !strings[i].contains("id") && strings[i+1].contains("id")) {

					stringToNull(strings, i);
				}

			}else if( i == j -1 && !strings[i-1].contains("id") && !strings[i].contains("id") ){

				stringToNull(strings, i);
			}

		}

	}

	/**
	 * Attempts to set the connection back to auto-commit, ignoring errors.
	 */
	private void safelySetAutoCommit() {
		try {
			conn.setAutoCommit(true);
		} catch (Exception e) {
		}
	}

	/**
	 * Attempts to close all the given resources, ignoring errors
	 *
	 * @param resources
	 */
	private void safelyClose(AutoCloseable... resources) {
		for (AutoCloseable resource : resources) {
			try {
				resource.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Prints the time difference from now to the input time.
	 */
	private void printTimeDiff(long time) {
		time = (System.currentTimeMillis() - time) / 1000;
		System.out.println("Took " + time + " seconds");
	}

	private String makeQueryFromThreeParams(String inputOne, String inputTwo, String inputThree) {
		return "SELECT * " +
				"FROM movies_social_network.movies " +
				"WHERE movies." + inputThree + "= ( " +
				"SELECT m3." + inputThree + " " +
				"FROM ( " +
				"SELECT m2." + inputThree + ", AVG(m2." + inputOne + ") as avg " +
				"FROM movies_social_network.movies AS m2 " +
				"WHERE m2." + inputOne + " > 0 " +
				"GROUP BY m2." + inputThree + " " +
				"ORDER BY avg " + inputTwo + ") AS m3 " +
				"LIMIT 1) " +
				"ORDER BY " + inputOne + " " + inputTwo + ";";
	}

	public List<UserId> getUsers() {
		List<UserId> users = new LinkedList<>();
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("select * from users");) {
			while (rs.next() == true) {
				UserId userId = new UserId();
				String userName = rs.getString("user_name");
				userId.setUserId(rs.getInt("user_id"));
				userId.setUserName(rs.getString("user_name"));
				try (PreparedStatement pstmt2 = conn.prepareStatement(QueriesName.userLikesAmount)) {
					pstmt2.setString(1, userName);
					ResultSet rs2 = pstmt2.executeQuery();
					if (rs2.next() == false) {
						userId.setUserLikes(0);
					} else {
						userId.setUserLikes(rs2.getInt("likes"));
					}
				}
				users.add(userId);
			}
		} catch (SQLException e) {
			System.out.println("ERROR executeQuery - " + e.getMessage());
		}
		return users;
	}
}

