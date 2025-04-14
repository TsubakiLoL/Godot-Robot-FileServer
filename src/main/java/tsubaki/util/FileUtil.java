package tsubaki.util;


import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import tsubaki.Loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantReadWriteLock;
@Service
public class FileUtil {


    public static ReentrantReadWriteLock lock=new ReentrantReadWriteLock();

    public static boolean isFileExists(String directoryPath, String fileName) {
        // 创建文件夹的File对象
        File directory = new File(directoryPath);

        // 检查该目录是否存在且是一个目录
        if (directory.exists() && directory.isDirectory()) {
            // 创建目标文件的File对象
            File targetFile = new File(directory, fileName);
            // 判断文件是否存在
            return targetFile.exists();
        }
        return false; // 如果目录不存在，返回false
    }


    public static void writeInputStreamtoOutputStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] b = new byte[1024];
        int length;
        while((length= inputStream.read(b))>0){
            outputStream.write(b,0,length);
        }
        inputStream.close();
        outputStream.close();
    }
    public static Boolean delete(String filePath) {

        File file = new File(filePath);
        System.out.println("文件目录"+filePath);
        if (file.exists()) {
            file.delete();
            System.out.println("===========删除成功=================");
            return true;
        } else {
            System.out.println("===============删除失败==============");
            return false;
        }
    }
    public static File createFileIfNotExists(String filePath) throws IOException {
        File file = new File(filePath);

        // 如果文件已存在，直接返回
        if (file.exists()) {
            return file;
        }

        // 确保父目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("无法创建父目录: " + parentDir.getAbsolutePath());
            }
        }

        // 尝试创建文件
        if (file.createNewFile()) {
            return file;
        }

        // 创建失败时再次检查文件是否存在（可能被其他线程/进程创建）
        if (file.exists()) {
            return file;
        } else {
            throw new IOException("文件创建失败: " + filePath);
        }
    }
    public static String getJarFilePath() {
        ApplicationHome home = new ApplicationHome(Loader.class);
        File jarFile = home.getSource();
        return jarFile.getParentFile().toString();
    }




    public static boolean createDirectoryIfNotExists(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            Files.createDirectories(path);
            return true;
        } catch (Exception e) {
            return false; // 所有异常（如路径非法、权限不足等）均静默返回失败
        }
    }

    public static boolean createDirJarPath(String dirpath){
        String path=getJarFilePath()+"/"+dirpath;
        return createDirectoryIfNotExists(path);
    }


    //获取适合当前操作系统格式的目录格式
    public static String getUsefulPath(String path){
        String path_abs=Paths.get(path).normalize().toAbsolutePath().toString();
        return path_abs;
    }
}