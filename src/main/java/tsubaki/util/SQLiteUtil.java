package tsubaki.util;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteUtil {
    /**
     * 创建 SQLite 数据库文件（如果不存在）
     * @param dbPath 数据库文件路径（如 "test.db" 或 "data/mydatabase.db"）
     * @return true 表示创建成功或已存在，false 表示失败
     */

    static String createSql="--\n" +
            "CREATE TABLE Author (\n" +
            "    author_id TEXT PRIMARY KEY\n" +
            "                   UNIQUE\n" +
            "                   NOT NULL,\n" +
            "    name      TEXT NOT NULL\n" +
            "                   UNIQUE,\n" +
            "    password  TEXT NOT NULL,\n" +
            "    head      TEXT NOT NULL\n" +
            ");\n" +
            "\n" +
            "CREATE TABLE NodeSet (\n" +
            "    set_id       TEXT PRIMARY KEY\n" +
            "                      UNIQUE\n" +
            "                      NOT NULL,\n" +
            "    author_id    TEXT REFERENCES Author (author_id) ON DELETE CASCADE\n" +
            "                                                    ON UPDATE CASCADE\n" +
            "                      NOT NULL,\n" +
            "    path         TEXT UNIQUE\n" +
            "                      NOT NULL,\n" +
            "    introduction TEXT NOT NULL,\n" +
            "    name         TEXT NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "CREATE TABLE Plugin (\n" +
            "    plugin_id    TEXT PRIMARY KEY\n" +
            "                      UNIQUE\n" +
            "                      NOT NULL,\n" +
            "    name         TEXT NOT NULL,\n" +
            "    author_id    TEXT NOT NULL\n" +
            "                      REFERENCES Author (author_id) ON DELETE CASCADE\n" +
            "                                                    ON UPDATE CASCADE,\n" +
            "    introduction TEXT NOT NULL\n" +
            ");\n" +
            "CREATE TABLE Version (\n" +
            "    plugin_id    TEXT REFERENCES Plugin (plugin_id) ON DELETE CASCADE\n" +
            "                                                    ON UPDATE CASCADE,\n" +
            "    version      TEXT NOT NULL,\n" +
            "    path         TEXT NOT NULL\n" +
            "                      UNIQUE,\n" +
            "    package_name TEXT NOT NULL,\n" +
            "    PRIMARY KEY (\n" +
            "        plugin_id,\n" +
            "        version\n" +
            "    )\n" +
            ");\n";
    public static boolean createSQLiteFile(String dbPath) {
        String path=FileUtil.getUsefulPath(dbPath);
        // 确保父目录存在（调用之前封装的 DirectoryUtils）
        String parentDir = Paths.get(path).getParent().toString();
        if (!FileUtil.createDirectoryIfNotExists(parentDir)) {
            System.out.println("创建数据库文件父目录失败");
            return false;
        }

        // JDBC 连接字符串（自动创建文件）
        String jdbcUrl = "jdbc:sqlite:" + path;

        System.out.println(path);
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement()) {
            System.out.println("执行sql结果"+String.valueOf(stmt.execute(createSql)));
            return true;
        } catch (SQLException e) {
            System.err.println("创建数据库失败: " + e.getMessage());
            return false;
        }
    }


}