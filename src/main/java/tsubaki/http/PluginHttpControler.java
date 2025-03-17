package tsubaki.http;



import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tsubaki.database.entity.Author;
import tsubaki.database.entity.Plugin;
import tsubaki.database.entity.Version;
import tsubaki.database.mapper.AuthorMapper;
import tsubaki.database.mapper.PluginMapper;
import tsubaki.database.mapper.VersionMapper;
import tsubaki.database.mybatis.GetSqlsession;
import tsubaki.util.FileUtil;
import tsubaki.util.MD5Util;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/plugin")
public class PluginHttpControler {


    @Value("${Http.download_path:D:\\}")
    private String path;

    //创建新的插件
    @PostMapping("/createPlugin")
    public ResponseEntity<?> handleCreatePlugin(MultipartHttpServletRequest request) {
        SqlSession sqlSession = GetSqlsession.getsqlsession();
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });
            //如果没有指明用户ID和密码，则返回失败
            if(!(formData.containsKey("author_id")&& formData.containsKey("password"))){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            if(!(formData.containsKey("plugin_name"))){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            String author_id=formData.get("author_id");
            String password=formData.get("password");
            String plugin_name=formData.get("plugin_name");
            String plugin_introduction="";
            if(formData.containsKey("introduction")){
                plugin_introduction=formData.get("introduction");
            }
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_id,password,authorMapper)){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            Map<String,String> result_map=new HashMap<String,String>();
            result_map.put("author_id",author_id);
            result_map.put("plugin_name",plugin_name);
            result_map.put("introduction",plugin_introduction);
            result_map.put("author_id",author_id);

            PluginMapper pluginMapper=sqlSession.getMapper(PluginMapper.class);
            String plugin_id=createPlugin(author_id,plugin_name,plugin_introduction,pluginMapper);
            result_map.put("plugin_id",plugin_id);
            sqlSession.close();
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            System.out.println("抛出错误"+e.toString());
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }

    //在插件的列表中创建新的版本实例（上传文件）
    @PostMapping("/uploadVersion")
    public ResponseEntity<?> handleUploadVersion(MultipartHttpServletRequest request) {
        SqlSession sqlSession = GetSqlsession.getsqlsession();
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });
            // 获取文件
            Map<String, MultipartFile> files = request.getFileMap();
            //如果没有指明用户ID和密码，则返回失败
            if(!(formData.containsKey("author_id")&& formData.containsKey("password"))){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            if(!(formData.containsKey("plugin_id")) || formData.containsKey("version") || files.size()!=1){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            String author_id=formData.get("author_id");
            String password=formData.get("password");
            String plugin_id=formData.get("plugin_id");
            String plugin_version=formData.get("version");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_id,password,authorMapper)){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            VersionMapper versionMapper=sqlSession.getMapper(VersionMapper.class);
            Version version=versionMapper.selectByIDAndVersion(plugin_id,plugin_version);

            PluginMapper pluginMapper=sqlSession.getMapper(PluginMapper.class);
            Plugin plugin=pluginMapper.selectByID(plugin_id);
            //如果不存在plugin或者plugin不属于该作者，返回失败
            if(plugin==null || plugin.getAuthor_id()!=author_id){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            //强转类型
            InputStream fileInputStream=((MultipartFile)(files.values().toArray()[0])).getInputStream();

            //结果数组
            Map<String,String> result_map=new HashMap<String,String>();
            //如果version存在，则替换,不对数据库进行更改
            if(version!=null){
                //写入之前文件
                Path filePath = Paths.get(path, version.getPath()).normalize().toAbsolutePath();
                File file = FileUtil.createFileIfNotExists(filePath.toString());
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                LockedFileOutputStream lockedFileOutputStream= new LockedFileOutputStream(fileOutputStream);
                FileUtil.writeInputStreamtoOutputStream(fileInputStream,lockedFileOutputStream);
                result_map.put("path",version.getPath());

            }
            //不存在则创建，并写入数据库
            else{
                String write_path=writeFile(fileInputStream,".zip");
                versionMapper.insertVersion(plugin_id,plugin_version,write_path);
                result_map.put("path",write_path);
            }



            result_map.put("author_id",author_id);
            result_map.put("plugin_id",plugin_id);
            result_map.put("version",plugin_version);


            sqlSession.close();
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            System.out.println("抛出错误"+e.toString());
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }


    //创建新的插件
    String createPlugin(String author_id,String plugin_name,String introduction, PluginMapper pluginMapper){
        UUID uuid = UUID.randomUUID();
        int max_search_times=100000;
        int times=0;
        while(pluginMapper.selectByID(uuid.toString())!=null && times<=max_search_times) {
            uuid=UUID.randomUUID();
            times+=1;
        }
        if(times==max_search_times){
            throw new RuntimeException("create_author_fail");
        }

        pluginMapper.insertPlugin(uuid.toString(),plugin_name,author_id,introduction);
        return uuid.toString();
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
