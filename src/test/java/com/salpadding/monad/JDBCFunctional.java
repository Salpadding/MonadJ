package com.salpadding.monad;

import java.sql.*;

public class JDBCFunctional {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/test";

    static final String USER = "root";
    static final String PASS = "123456";

    public static void main(String[] args) {
        Monad.of(JDBC_DRIVER).map(Class::forName) // load driver
                .map(c -> DriverManager.getConnection(DB_URL, USER, PASS)).onClean(Connection::close) // get connection
                .map(Connection::createStatement).onClean(Statement::close) // get statement and clean resource
                .map(s -> s.executeQuery("SELECT * FROM crawler_article")).onClean(ResultSet::close) // get result and clean resource
                .ifPresent((rs) -> {
                    while (rs.next()) {
                        String title = rs.getString("title");
                        String author = rs.getString("author");
                        System.out.println(title + ":" + author);
                    }
                }).except(Throwable::printStackTrace).cleanUp(); // print error message and clean up
    }
}
