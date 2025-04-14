package tsubaki;
//引导类，spring boot项目入口

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ApplicationContext;
import tsubaki.util.FileUtil;
import tsubaki.util.SQLiteUtil;
import tsubaki.util.SystemUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class Loader {

    private static List<String> needCreateDir= Arrays.asList("download","userSandBox");
    private  static ApplicationContext appContext;
    public static void main(String[] args) {
        for (String s : needCreateDir) {
            String path=FileUtil.getJarFilePath()+"/"+s;
            System.out.println("尝试创建目录:"+path);
            boolean res=FileUtil.createDirectoryIfNotExists(path);
            System.out.println(String.valueOf(res));
        }



        appContext=SpringApplication.run(Loader.class,args);
    }

    public static ApplicationContext getContext(){
        return appContext;
    }



}
