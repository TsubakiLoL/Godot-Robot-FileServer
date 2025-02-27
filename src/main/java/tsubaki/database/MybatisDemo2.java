package tsubaki.database;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import tsubaki.database.entity.Author;
import tsubaki.database.mapper.AuthorMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MybatisDemo2 {
    public static void main(String[] args) throws IOException {
        String resource ="mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory =new SqlSessionFactoryBuilder().build(inputStream);

        SqlSession sqlSession = sqlSessionFactory.openSession();


        AuthorMapper authorMapper=sqlSession.getMapper(AuthorMapper.class);

        List<Author> authors=authorMapper.selectAll();
        //List<Author> authors= sqlSession.selectList("Author.selectAll");
        System.out.println(authors);
        System.out.println(authorMapper.selectByID("11"));
        System.out.println(authorMapper.selectByName("测试"));
        sqlSession.close();
    }
}
