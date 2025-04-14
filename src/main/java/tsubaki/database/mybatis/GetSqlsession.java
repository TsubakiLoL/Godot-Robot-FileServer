package tsubaki.database.mybatis;


import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import tsubaki.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GetSqlsession {
    private static InputStream inputStream;

    private static  SqlSessionFactory sqlSessionFactory=createfactory();

    //这个方法生成一个生产Sqlsession的工厂，即SqlSessionFactory
    public static SqlSessionFactory createfactory() {

        Properties props = null;
        try {
            String dynamicUrl = "jdbc:sqlite:"+FileUtil.getUsefulPath(FileUtil.getJarFilePath()+"/"+"data.db");

            System.out.println(dynamicUrl);
            // 创建 Properties 并设置 URL
            props = new Properties();
            props.setProperty("jdbc.url", dynamicUrl);

            // 加载 XML 配置并注入属性
            inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        } catch (IOException e) {
            System.out.println("构建出错");
            e.printStackTrace();

        }

        //通过把这个工厂return出来，以便后续通过这个工厂获得SqlSession对象
        return new SqlSessionFactoryBuilder().build(inputStream, props);
    }

    //这个方法获得SqlSession对象
    public static SqlSession getsqlsession(){
        return sqlSessionFactory.openSession(true);
    }


}

