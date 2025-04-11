package tsubaki;
//引导类，spring boot项目入口

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ApplicationContext;

import java.io.File;

@SpringBootApplication
public class Loader {

    private  static ApplicationContext appContext;
    public static void main(String[] args) {
        appContext=SpringApplication.run(Loader.class,args);
    }

    public static ApplicationContext getContext(){
        return appContext;
    }



}
