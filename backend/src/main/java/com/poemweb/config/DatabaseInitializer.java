package com.poemweb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Component
@Order(1)
public class DatabaseInitializer implements CommandLineRunner {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Override
    public void run(String... args) throws Exception {
        if (url.contains("poem_web")) {
            String baseUrl = url.replace("/poem_web?", "/?");
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(baseUrl, username, password);
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
}