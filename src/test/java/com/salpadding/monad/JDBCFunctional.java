package com.salpadding.monad;

import java.sql.Connection;
import java.sql.DriverManager;

public class JDBCFunctional {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/test";

    static final String USER = "root";
    static final String PASS = "123456";

    public static void main(String[] args) {
        Monad.of(JDBC_DRIVER).map(Class::forName) // load driver
                .map(c -> DriverManager.getConnection(DB_URL, USER, PASS))// get connection
                .map(Connection::createStatement) // get statement and clean resource
                .map(s -> s.executeQuery("SELECT * FROM crawler_article"))// get result and clean resource
                .peek((rs) -> {
                    while (rs.next()) {
                        String title = rs.getString("title");
                        String author = rs.getString("author");
                        System.out.println(title + ":" + author);
                    }
                }).except(Throwable::printStackTrace); // print error message and clean up
    }
}
