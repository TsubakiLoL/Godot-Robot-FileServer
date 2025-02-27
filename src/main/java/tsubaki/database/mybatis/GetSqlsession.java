package tsubaki.database.mybatis;


import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class GetSqlsession {
    private static InputStream inputStream;

    //这个方法生成一个生产Sqlsession的工厂，即SqlSessionFactory
    public static SqlSessionFactory createfactory() {

        {
            try {
                inputStream = Resources.getResourceAsStream("mybatisconfig.xml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //通过把这个工厂return出来，以便后续通过这个工厂获得SqlSession对象
        return sqlSessionFactory;
    }

    //这个方法获得SqlSession对象
    public static SqlSession getsqlsession(){
        return createfactory().openSession();
    }


}

