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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileHttpControler {


    @Value("${Http.download_path:D:\\}")
    private String path;


    // 从服务器文件系统下载文件
    @GetMapping("/download/{path}")
    public ResponseEntity<InputStreamResource> downloadFromServer(@PathVariable(value="path") String r_path) throws IOException {
        Path filePath = Paths.get(path, r_path).normalize().toAbsolutePath();
        if (!filePath.startsWith(path)) {
            System.out.println(path+" :"+r_path+":"+filePath);
            System.out.println("Error:路径无权访问");
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


}


