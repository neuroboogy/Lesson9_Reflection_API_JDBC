package com.company;

import org.sqlite.JDBC;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.*;

public class Main {
    static Connection connection;
    static Statement statement;

    public static void main(String[] args) {
        System.out.println("Lesson 9");

        Object[] objects = {new Student("Bob", 18, 10, 180, 90),
                new Student("Nick",22,15, 170, 70),
                new Teacher("Jack",45,30, 175, 80),
                new Student("Mike", 32,20, 194, 85),
                new Teacher("Jim",50,50, 174, 65),
        };

        List<Student> students = Arrays.stream(objects)
                .filter((x) -> x.getClass().isAnnotationPresent(Table.class))
                .filter((x) -> x.getClass().getAnnotation(Table.class).title().equals("Students"))
                .map((x) -> (Student)x)
                .collect(Collectors.toList());

        students.forEach(x -> System.out.println(x.getName() + " | " + "age : " + x.getAge()));


        String tableTitle = Student.class.getAnnotation(Table.class).title();

        List<String> annotationNames = Stream.of(Student.class.getDeclaredMethods())
                .filter(x -> x.isAnnotationPresent(Column.class))
                .map(x -> x.getAnnotation(Column.class).title())
                .collect(Collectors.toList());
//                .forEach(x -> System.out.println(x.getName() + " : annotation : " + x.getAnnotation(Column.class).title()));

        String createTablePrefix = "CREATE TABLE IF NOT EXISTS " + tableTitle + " (\n";
        String createTableSuffix = "\n);";

        String createTableContent = "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n";

        for (int i=0; i<annotationNames.size(); i++) {
            createTableContent += "  " + annotationNames.get(i) + " TEXT";
            if (i != annotationNames.size()-1) {
                createTableContent += ",\n";
            }
        }

        String createTableRequest = createTablePrefix + createTableContent + createTableSuffix;
        System.out.println(createTableRequest);


        try {
            connect();
//            statement.executeUpdate("INSERT INTO students (name, score) VALUES ('Bob3', 100);");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS teachers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, score INTEGER, level INTEGER);");

            statement.executeUpdate("DROP TABLE IF EXISTS Students;");
            statement.executeUpdate(createTableRequest);

            String insertPrefix = "INSERT INTO " + tableTitle;
            students.stream()
                    .filter(o -> o.getClass().isAnnotationPresent(Table.class))
                    .forEach(o -> {
                        System.out.println(o.getName());
                        Map<String, String> columnMap = Stream.of(o.getClass().getDeclaredMethods())
                                .filter(m -> m.isAnnotationPresent(Column.class))
                                .collect(Collectors.toMap(m -> m.getAnnotation(Column.class).title(), m -> {
                                    try {
                                        return m.invoke(o).toString();
                                    } catch (IllegalAccessException | InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }));
                        for (String key : columnMap.keySet()) {
                            System.out.println(key + " " + columnMap.get(key));
                        }

                        String insertParams = "";
                        String insertValues = "";
                        int i = 0;
                        for (String key : columnMap.keySet()) {
                            insertParams += key;
                            insertValues += "'" + columnMap.get(key) + "'";
                            i++;
                            if (i < columnMap.size()) {
                                insertParams += ",";
                                insertValues += ",";
                            }
                        }
                        String insertRequest = insertPrefix + " (" + insertParams + ") " + "VALUES (" + insertValues + ") ";
                        System.out.println(insertRequest);

                        try {
                            statement.executeUpdate(insertRequest);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public static void connect() throws SQLException {
        try {
            Class.forName( "org.sqlite.JDBC" );
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new SQLException("Unable to connect");
        }
    }

    public static void disconnect() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
