package tsubaki.http;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
            resource = new InputStreamResource(Files.newInputStream(filePath));
        } catch (IOException e) {
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

    //上传文件
    @PostMapping("/upload")
    public String save(@RequestParam Map<String, Object> map){

        System.out.println("书名：" + map.get("name") + ", 作者: " + map.get("author"));

        return "书名：" + map.get("name") + ", 作者: " + map.get("author");
    }


}


