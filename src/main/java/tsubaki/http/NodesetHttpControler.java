package tsubaki.http;



import com.google.gson.Gson;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tsubaki.database.entity.Author;
import tsubaki.database.entity.NodeSet;
import tsubaki.database.entity.Plugin;
import tsubaki.database.entity.Version;
import tsubaki.database.mapper.AuthorMapper;
import tsubaki.database.mapper.NodeSetMapper;
import tsubaki.database.mapper.PluginMapper;
import tsubaki.database.mapper.VersionMapper;
import tsubaki.database.mybatis.GetSqlsession;
import tsubaki.util.FileUtil;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/nodeset")
public class NodesetHttpControler {


    @Value("${Http.download_path:D:\\}")
    private String path;


    @Value("${Http.server_address:http://localhost:8080}")
    private String server_address;
    //创建新的插件
    @PostMapping("/create")
    public ResponseEntity<?> handleCreateNodeset(MultipartHttpServletRequest request) {
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
            if(!(formData.containsKey("author_name")&& formData.containsKey("password"))){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            if(!(formData.containsKey("nodeset_name"))){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            String nodeset_name=formData.get("nodeset_name");
            String nodeset_introduction="";
            if(formData.containsKey("introduction")){
                nodeset_introduction=formData.get("introduction");
            }
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            Map<String,String> result_map=new HashMap<String,String>();
            result_map.put("author_name",author_name);
            result_map.put("nodeset_name",nodeset_name);
            result_map.put("introduction",nodeset_introduction);

            //强转类型，获取输入文件流
            InputStream fileInputStream=((MultipartFile)(files.values().toArray()[0])).getInputStream();

            String file_path=writeFile(fileInputStream,".nodeset");
            NodeSetMapper nodeSetMapper=sqlSession.getMapper(NodeSetMapper.class);
            Author author=authorMapper.selectByNameUnique(author_name);
            String nodeset_id=createNodeset(author.getAuthor_id(),nodeset_name,nodeset_introduction,file_path,nodeSetMapper);
            result_map.put("nodeset_id",nodeset_id);
            sqlSession.close();
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            System.out.println("抛出错误"+e.toString());
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }



    @PostMapping("/delete")
    public ResponseEntity<?> handleDeleteNodeset(MultipartHttpServletRequest request) {
        SqlSession sqlSession = GetSqlsession.getsqlsession();
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });
            //如果没有指明用户ID和密码，则返回失败
            if(!(formData.containsKey("author_name")&& formData.containsKey("password"))){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            if(!(formData.containsKey("nodeset_id"))){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            String nodeset_id=formData.get("nodeset_id");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            Map<String,String> result_map=new HashMap<String,String>();
            result_map.put("author_name",author_name);
            result_map.put("nodeset_id",nodeset_id);


            NodeSetMapper nodeSetMapper=sqlSession.getMapper(NodeSetMapper.class);


            NodeSet nodeSet=nodeSetMapper.selectBySetID(nodeset_id);
            if(nodeSet==null){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            Author author=authorMapper.selectByNameUnique(author_name);
            //非本人拥有
            if(!nodeSet.getAuthor_id().equals(author.getAuthor_id())){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }

            nodeSetMapper.deleteNodeSetBySetID(nodeset_id);
            sqlSession.close();
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            System.out.println("抛出错误"+e.toString());
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }



    @PostMapping("/search")
    public ResponseEntity<?> handleSearchNodeset(MultipartHttpServletRequest request) {
        SqlSession sqlSession = GetSqlsession.getsqlsession();
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });
            if(!(formData.containsKey("content"))){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            String content=formData.get("content");
            Map result_map=new HashMap();
            result_map.put("content",content);
            NodeSetMapper nodeSetMapper=sqlSession.getMapper(NodeSetMapper.class);
            AuthorMapper authorMapper=sqlSession.getMapper(AuthorMapper.class);
            List<NodeSet> nodeSets=nodeSetMapper.selectByName("%"+content+"%");
            Map[] cache_array=new Map[nodeSets.size()];
            for(int i=0;i<nodeSets.size();i++){
                Map cache=new HashMap<>();
                NodeSet p= nodeSets.get(i);
                cache.put("nodeset_id",p.getSet_id());
                cache.put("nodeset_name",p.getName());
                cache.put("author_id",p.getAuthor_id());
                Author author=authorMapper.selectByID(p.getAuthor_id());
                cache.put("introduction",p.getIntroduction());
                cache.put("author_name",author.getName());
                cache_array[i]=cache;
            }
            result_map.put("nodeset",cache_array);
            sqlSession.close();
            Gson gson=new Gson();
            System.out.println(gson.toJson(result_map));
            return  ResponseEntity.ok(gson.toJson(result_map));
        } catch (Exception e) {
            System.out.println("抛出错误"+e.toString());
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }


    @PostMapping("/get")
    public ResponseEntity<?> handleGetNodeset(MultipartHttpServletRequest request) {
        SqlSession sqlSession = GetSqlsession.getsqlsession();
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });
            if(!(formData.containsKey("nodeset_id"))){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            String nodeset_id=formData.get("nodeset_id");
            Map result_map=new HashMap();
            result_map.put("nodeset_id",nodeset_id);
            NodeSetMapper nodeSetMapper=sqlSession.getMapper(NodeSetMapper.class);
            NodeSet nodeSet=nodeSetMapper.selectBySetID(nodeset_id);
            if(nodeSet==null){
                sqlSession.close();
                return ResponseEntity.ok("Fail");
            }
            result_map.put("nodeset_id",nodeSet.getSet_id());
            result_map.put("nodeset_name",nodeSet.getName());
            result_map.put("author_id",nodeSet.getAuthor_id());
            result_map.put("introduction",nodeSet.getIntroduction());
            result_map.put("path",server_address+"/files/download/"+nodeSet.getPath());
            sqlSession.close();
            Gson gson=new Gson();
            System.out.println(gson.toJson(result_map));
            return  ResponseEntity.ok(gson.toJson(result_map));
        } catch (Exception e) {
            System.out.println("抛出错误"+e.toString());
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }

    //创建新的节点集
    String createNodeset(String author_id,String set_name,String introduction, String file_path,NodeSetMapper nodeSetMapper){
        UUID uuid = UUID.randomUUID();
        int max_search_times=100000;
        int times=0;
        while(nodeSetMapper.selectBySetID(uuid.toString())!=null && times<=max_search_times) {
            uuid=UUID.randomUUID();
            times+=1;
        }
        if(times==max_search_times){
            throw new RuntimeException("create_author_fail");
        }

        nodeSetMapper.addNodeSet(uuid.toString(),author_id,introduction,set_name,file_path);
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
