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
import tsubaki.database.mapper.NodeSetMapper;
import tsubaki.database.mybatis.GetSqlsession;
import tsubaki.util.FileUtil;
import tsubaki.util.MD5Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/image")
public class ImageHttpControler {

    @Value("${Http.download_path:D:\\}")
    private String path;


    @Value("${Http.server_address:http://localhost:8080}")
    private String server_address;
    //上传图片
    @PostMapping("/upload")
    public ResponseEntity<?> handleUploadImage(MultipartHttpServletRequest request) {
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });
            if(!formData.containsKey("backend")){
                return ResponseEntity.ok("Fail");
            }
            String backend=formData.get("backend");
            // 获取文件
            Map<String, MultipartFile> files = request.getFileMap();
            //结果字典
            Map<String,String> result_map =new HashMap<String,String>();
            //强转类型，获取输入文件流
            InputStream fileInputStream=((MultipartFile)(files.values().toArray()[0])).getInputStream();

            if(!backend.startsWith(".")){
                backend="."+backend;
            }
            String file_path=writeFile(fileInputStream,backend);
            result_map.put("path",server_address+"/files/download/"+file_path);
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Fail");

        }
    }
    //尝试写入文件到指定文件夹下，返回写入文件的路径
    String writeFile(InputStream inputStream,String back) throws IOException {
        UUID uuid=UUID.randomUUID();
        int max_search_times=100000;
        int times=0;
        while(FileUtil.isFileExists(path,uuid.toString().replace("-","")+back) && times<=max_search_times)
        {
            uuid=UUID.randomUUID();
            times+=1;
        }
        Path filePath = Paths.get(path, uuid.toString().replace("-","")+back).normalize().toAbsolutePath();


        File file = FileUtil.createFileIfNotExists(filePath.toString());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        LockedFileOutputStream lockedFileOutputStream= new LockedFileOutputStream(fileOutputStream);
        FileUtil.writeInputStreamtoOutputStream(inputStream,lockedFileOutputStream);
        return uuid.toString().replace("-","")+back;
    }

}
