package com.salpadding.monad;

import java.sql.*;

public class JDBC {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/test";

    //第三步：说明数据库的认证账户及密码
    static final String USER = "root";
    static final String PASS = "123456";
    public static void main(String[] args) {
        //第四步：注册JDBC驱动
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            //这里会发生类没有找到的异常！
            e.printStackTrace();
            return;
        }
        //第五步：获得数据库连接
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection =  DriverManager.getConnection(DB_URL,USER,PASS);
            //第六步：执行查询语句
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
