package com.poemweb;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@SpringBootApplication
@ComponentScan(basePackages = "com.poemweb")
public class PoemWebApplication {
    static {
        createDatabase();
    }

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(PoemWebApplication.class, args);
    }

    private static void createDatabase() {
        String url = "jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "123456";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS poem_web CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            stmt.close();
            conn.close();
            System.out.println("数据库 poem_web 创建成功或已存在");
        } catch (Exception e) {
            System.err.println("创建数据库失败: " + e.getMessage());
        }
    }
}