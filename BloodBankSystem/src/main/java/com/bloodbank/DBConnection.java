package com.bloodbank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates the JDBC connection to the Oracle database.
 * Change URL, USER and PASSWORD to match your own Oracle setup.
 */
public class DBConnection {

    // Oracle XE 18c / 21c (pluggable database):
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
    // Oracle XE 11g or a classic SID instead? Use this one:
    // private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";

    private static final String USER = "BloodBank";
    private static final String PASSWORD = "BloodBank";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
