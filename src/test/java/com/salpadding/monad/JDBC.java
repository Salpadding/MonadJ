package com.salpadding.monad;

import java.sql.*;

public class JDBC {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/test";

    static final String USER = "root";
    static final String PASS = "123456";
    public static void main(String[] args) {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection =  DriverManager.getConnection(DB_URL,USER,PASS);
            statement = connection.createStatement();
            String sql = "SELECT * FROM crawler_article";
            rs = statement.executeQuery(sql);
            while (rs.next())
            {
                String title = rs.getString("title");
                String author = rs.getString("author");
                System.out.println(title+":"+author);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try{
                rs.close();
            }catch (Exception e){
            }
            try{
                statement.close();
            }catch (Exception e){

            }
            try{
                connection.close();
            }catch (Exception e){

            }
        }
    }
}
