package com.movies.movies;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class ConnectionToDB {
    private String user;
    private String password;
    private String schema;
    private String host;
    private String port;

    public ConnectionToDB() throws IOException {
        String configFilePath = "src/config.properties";
        FileInputStream propsInput = new FileInputStream(configFilePath);
        Properties prop = new Properties();
        prop.load(propsInput);
        this.user = prop.getProperty("DB_USER");
        this.password = prop.getProperty("DB_PASSWORD");
        this.schema = prop.getProperty("DB_SCHEMA");
        this.host = prop.getProperty("DB_HOST");
        this.port = prop.getProperty("DB_PORT");
    }

    /**
     *
     * @return connection to DB if the connection was successfully set
     */
    public Connection openConnection() {
        Connection conn = null;
        // loading the driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Unable to load the MySQL JDBC driver..");
            return conn;
        }
        System.out.println("Driver loaded successfully");

        System.out.print("Trying to connect... ");

        try {
            conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + schema, user, password);
        } catch (SQLException e) {
            System.out.println("Unable to connect - " + e.getMessage());
            return null;
        }
        System.out.println("Connected!");
        return conn;
    }

    /**
     * close the connection
     */
    public void closeConnection(Connection conn) {
        // closing the connection
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println("Unable to close the connection - " + e.getMessage());
        }

    }
}
