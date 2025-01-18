package tsubaki.databse;

import java.time.LocalDateTime;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("DBtest")

public class DBtest {

    @Autowired
    JdbcTemplate jdbcTemplate;


    public void test() throws Exception {
        // 1、首先创建数据表
        String ddl = """
            CREATE TABLE `user` (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT,
                create_at TEXT
            );
        """;

        this.jdbcTemplate.execute(ddl);

        // 2、插入一条数据
        int ret = this.jdbcTemplate.update("INSERT INTO `user` (`id`, `name`, `create_at`) VALUES (?, ?, ?);", new Object[] {1, "springdoc", LocalDateTime.now()});

        System.out.println(("插入数据：{}"+ret));

        // 3、检索一条数据
        Map<String, Object> user = this.jdbcTemplate.queryForObject("SELECT * FROM `user` WHERE `id` = ?", new ColumnMapRowMapper(), 1L);

        System.out.println(("检索数据：{}"+ user));
    }
}