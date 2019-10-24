# Monad

## Annoying by try&catch code looks like following ?

```java
package com.salpadding.exceptional;

import java.sql.*;

public class JDBC {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/test";

    static final String USER = "root";
    static final String PASS = "123456";
    public static void main(String[] args) {
        // load mysql driver
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
            // get connection
            connection =  DriverManager.getConnection(DB_URL,USER,PASS);
            // execute query
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
            // clear resource
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

```

## Avoid try-catch by Monad api!

```java
package com.salpadding.exceptional;

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
```

