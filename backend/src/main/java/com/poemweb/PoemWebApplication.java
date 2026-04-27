package com.poemweb;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@SpringBootApplication
@ComponentScan(basePackages = "com.poemweb")
public class PoemWebApplication {
    static {
        createDatabase();
        migratePoemTable();
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

    // 为 poem 表添加 author, dynasty, tag 字段并从知识图谱回填数据
    private static void migratePoemTable() {
        String url = "jdbc:mysql://localhost:3306/poem_web?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "123456";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();

            // 1. 检查并添加 author 字段
            if (!columnExists(stmt, "poem", "author")) {
                stmt.executeUpdate("ALTER TABLE poem ADD COLUMN author VARCHAR(50) DEFAULT NULL COMMENT '作者姓名'");
                System.out.println("已添加 poem.author 字段");
            }
            // 2. 检查并添加 dynasty 字段
            if (!columnExists(stmt, "poem", "dynasty")) {
                stmt.executeUpdate("ALTER TABLE poem ADD COLUMN dynasty VARCHAR(20) DEFAULT NULL COMMENT '朝代'");
                System.out.println("已添加 poem.dynasty 字段");
            }
            // 3. 检查并添加 tag 字段
            if (!columnExists(stmt, "poem", "tag")) {
                stmt.executeUpdate("ALTER TABLE poem ADD COLUMN tag VARCHAR(50) DEFAULT NULL COMMENT '题材标签'");
                System.out.println("已添加 poem.tag 字段");
            }

            // 4. 从知识图谱回填数据（仅回填空值）
            stmt.executeUpdate(
                "UPDATE poem p LEFT JOIN graph_node an ON p.author_id = an.id SET p.author = an.name " +
                "WHERE p.author IS NULL AND p.author_id IS NOT NULL");

            stmt.executeUpdate(
                "UPDATE poem p LEFT JOIN graph_node dn ON p.dynasty_id = dn.id SET p.dynasty = dn.name " +
                "WHERE p.dynasty IS NULL AND p.dynasty_id IS NOT NULL");

            stmt.executeUpdate(
                "UPDATE poem p LEFT JOIN graph_node tn ON p.tag_id = tn.id SET p.tag = tn.name " +
                "WHERE p.tag IS NULL AND p.tag_id IS NOT NULL");

            stmt.close();
            conn.close();
            System.out.println("诗词表字段迁移和数据回填完成");
        } catch (Exception e) {
            System.err.println("诗词表字段迁移失败: " + e.getMessage());
        }
    }

    private static boolean columnExists(Statement stmt, String table, String column) {
        try {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'poem_web' AND TABLE_NAME = '" + table + "' AND COLUMN_NAME = '" + column + "'");
            boolean exists = rs.next();
            rs.close();
            return exists;
        } catch (Exception e) {
            return false;
        }
    }
}