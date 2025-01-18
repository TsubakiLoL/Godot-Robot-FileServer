package tsubaki;
//引导类，spring boot项目入口

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ApplicationContext;
import tsubaki.databse.DBtest;

import java.io.File;

@SpringBootApplication
public class Loader {

    private  static ApplicationContext appContext;
    public static void main(String[] args) {
        appContext=SpringApplication.run(Loader.class,args);
        System.out.println(getJarFilePath());
        try {
            ((DBtest)appContext.getBean("DBtest")).test();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ApplicationContext getContext(){
        return appContext;
    }

    public static String getJarFilePath() {
        ApplicationHome home = new ApplicationHome(Loader.class);
        File jarFile = home.getSource();
        return jarFile.getParentFile().toString();
    }


}
