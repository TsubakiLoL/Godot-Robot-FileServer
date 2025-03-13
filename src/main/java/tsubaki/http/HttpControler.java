package tsubaki.http;


import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tsubaki.database.entity.Author;
import tsubaki.database.mapper.AuthorMapper;
import tsubaki.database.mybatis.GetSqlsession;
import tsubaki.util.MD5Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class HttpControler {


    @Value("${Http.download_path:D:\\}")
    private String path;
    // 从服务器文件系统下载文件
    @GetMapping("/download/{path}")
    public ResponseEntity<InputStreamResource> downloadFromServer(@PathVariable(value="path") String r_path) throws IOException {
        Path filePath = Paths.get(path, r_path).normalize().toAbsolutePath();
        if (!filePath.startsWith(path)) {
            System.out.printf("Error:路径无权访问");
            return ResponseEntity.status(403).build(); // 路径越权检查
        }

        File file = filePath.toFile();
        //建立输入流
        InputStreamResource resource ;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            resource = new InputStreamResource(new LockedFileInputStream(fileInputStream));
        } catch (Exception e) {

            //当文件不存在时返回404，并进行标准输出
            System.out.printf("Error:文件[%s]不存在\n",file.getPath());
            return ResponseEntity.status(404).build();

        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }

    //请求访问列表
    @GetMapping("/search/{path}")
    public ResponseEntity<InputStreamResource> downloadFromServer(@PathVariable(value="path") String r_path,@RequestParam("type") String type ) throws IOException {
        Path filePath = Paths.get(path, r_path).normalize().toAbsolutePath();
        if (!filePath.startsWith(path)) {
            return ResponseEntity.status(403).build(); // 路径越权检查
        }

        File file = filePath.toFile();
        System.out.println(type);
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleUpload(MultipartHttpServletRequest request) {
        // 获取普通参数
        Map<String, String> formData = new HashMap<>();
        System.out.println( request.getParameterMap().toString());
        System.out.println(request.getParameterMap().isEmpty());
        System.out.println(request.getParameterMap().size());
        request.getParameterMap().forEach((key, values) -> {
            formData.put(key, values.length > 0 ? values[0] : null);
        });
        // 获取文件
        Map<String, MultipartFile> files = request.getFileMap();
        System.out.println(files);
        return ResponseEntity.ok(Map.of("formData", formData, "files", files.keySet()));
    }


    //登录查询
    @PostMapping("/login")
    public ResponseEntity<?> handleLogin(MultipartHttpServletRequest request) {
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });

            if (!formData.containsKey("id") || !formData.containsKey("password")) {
                System.out.println("请求参数不全");
                return ResponseEntity.ok("Fail");
            }
            String id = formData.get("id");
            String password = formData.get("password");


            SqlSession sqlSession = GetSqlsession.getsqlsession();
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            Author author = authorMapper.selectByID(id);
            sqlSession.close();
            if (author == null) {
                System.out.println("未找到用户");

                return ResponseEntity.ok("Fail");

            }
            String MD5 = MD5Util.generateMD5(author.getPassword());

            if (!(MD5.equals( password))) {
                System.out.println("密码不正确:"+MD5+","+password);
                return ResponseEntity.ok("Fail");
            }
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.out.println("抛出错误"+e.toString());
            return ResponseEntity.ok("Fail");

        }
    }




    //注册
    @PostMapping("/signup")
    public ResponseEntity<?> handleSignup(MultipartHttpServletRequest request) {
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });

            if (!formData.containsKey("name") || !formData.containsKey("password")) {
                System.out.println("请求参数不全");
                return ResponseEntity.ok("Fail");
            }
            String name = formData.get("name");
            String password = formData.get("password");


           String author_id=signUpAuthor(name,password);
           return ResponseEntity.ok(Map.of("id",author_id,"name",name,"password",password));
        } catch (Exception e) {
            System.out.println("抛出错误"+e.toString());
            return ResponseEntity.ok("Fail");

        }
    }




    //注册用户返回ID
    String signUpAuthor(String name,String password){
        SqlSession sqlSession = GetSqlsession.getsqlsession();
        AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
        UUID uuid = UUID.randomUUID();
        int max_search_times=100000;
        int times=0;
        while(authorMapper.selectByID(uuid.toString())==null && times<=max_search_times) {
            uuid=UUID.randomUUID();
            times+=1;
        }
        if(times==max_search_times){
            sqlSession.close();
            throw new RuntimeException("create_author_fail");

        }
        authorMapper.addAuthor(uuid.toString(),name,password);
        System.out.println(authorMapper.selectAll().toString());
        sqlSession.commit();
        sqlSession.close();

        return uuid.toString();

    }



    @PostMapping("/updateAuthor")
    public ResponseEntity<?> handleUpdateAuthor(MultipartHttpServletRequest request) {
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });

            if(formData.containsKey("name") && formData.containsKey("password")){


            }
            String name = formData.get("name");
            String password = formData.get("password");


            String author_id=signUpAuthor(name,password);
            return ResponseEntity.ok(Map.of("id",author_id,"name",name,"password",password));
        } catch (Exception e) {
            System.out.println("抛出错误"+e.toString());
            return ResponseEntity.ok("Fail");

        }
    }



}


