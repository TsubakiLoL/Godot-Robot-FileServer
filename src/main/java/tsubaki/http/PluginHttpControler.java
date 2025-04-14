package tsubaki.http;



import com.google.gson.Gson;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import tsubaki.file.LockedFileOutputStreamFix;
import tsubaki.util.FileUtil;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/plugin")
public class PluginHttpControler {


    //@Value("${Http.download_path:D:\\}")
    private String path;


    @Value("${Http.server_address:http://localhost:8080}")
    private String server_address;

    public PluginHttpControler(){
        path=FileUtil.getJarFilePath()+"/download/";
    }
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
            if(!(formData.containsKey("author_name")&& formData.containsKey("password"))){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            if(!(formData.containsKey("plugin_name"))){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            String plugin_name=formData.get("plugin_name");
            String plugin_introduction="";
            if(formData.containsKey("introduction")){
                plugin_introduction=formData.get("introduction");
            }
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            Author author=authorMapper.selectByNameUnique(author_name);
            Map<String,String> result_map=new HashMap<String,String>();
            result_map.put("author_name",author_name);
            result_map.put("plugin_name",plugin_name);
            result_map.put("introduction",plugin_introduction);

            PluginMapper pluginMapper=sqlSession.getMapper(PluginMapper.class);
            String plugin_id=createPlugin(author.getAuthor_id(),plugin_name,plugin_introduction,pluginMapper);
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
            if(!(formData.containsKey("author_name")&& formData.containsKey("password"))){
                sqlSession.close();
                System.out.println("参数缺少");
                System.out.println(formData.toString());
                return ResponseEntity.ok().body("Fail");
            }
            if((!formData.containsKey("plugin_id")) || (!formData.containsKey("version") ) ||  (!formData.containsKey("package_name")) || files.size()!=1){
                sqlSession.close();
                System.out.println(formData.toString());
                return ResponseEntity.ok().body("Fail");
            }
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            String plugin_id=formData.get("plugin_id");
            String plugin_version=formData.get("version");
            //获取包名
            String package_name=formData.get("package_name");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                System.out.println("密码验证不通过");
                return ResponseEntity.ok().body("Fail");
            }
            VersionMapper versionMapper=sqlSession.getMapper(VersionMapper.class);
            Version version=versionMapper.selectByIDAndVersion(plugin_id,plugin_version);

            Author author=authorMapper.selectByNameUnique(author_name);
            PluginMapper pluginMapper=sqlSession.getMapper(PluginMapper.class);
            Plugin plugin=pluginMapper.selectByID(plugin_id);
            //如果不存在plugin或者plugin不属于该作者，返回失败
            if(plugin==null || !plugin.getAuthor_id().equals(author.getAuthor_id())){
                sqlSession.close();
                System.out.println("不属于该作者");
                System.out.println(plugin.getAuthor_id());
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
                //FileOutputStream fileOutputStream = new FileOutputStream(file);
                LockedFileOutputStreamFix lockedFileOutputStreamFix=new LockedFileOutputStreamFix(filePath.toString());
                //LockedFileOutputStream lockedFileOutputStream= new LockedFileOutputStream(fileOutputStream);
                FileUtil.writeInputStreamtoOutputStream(fileInputStream,lockedFileOutputStreamFix);
                result_map.put("path",version.getPath());
            }
            //不存在则创建，并写入数据库
            else{
                String write_path=writeFile(fileInputStream,".zip");
                versionMapper.insertVersion(plugin_id,plugin_version,write_path,package_name);
                result_map.put("path",write_path);
            }
            result_map.put("author_name",author_name);
            result_map.put("plugin_id",plugin_id);
            result_map.put("version",plugin_version);


            sqlSession.close();
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            e.printStackTrace();
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
        //FileOutputStream fileOutputStream = new FileOutputStream(file);
        LockedFileOutputStreamFix lockedFileOutputStreamFix=new LockedFileOutputStreamFix(filePath.toString());
        //LockedFileOutputStream lockedFileOutputStream= new LockedFileOutputStream(fileOutputStream);
        FileUtil.writeInputStreamtoOutputStream(inputStream,lockedFileOutputStreamFix);
        return uuid.toString().replace("-","")+back;
    }



    //通过作者ID获取插件全部信息
    //如果查找到返回的应该是如下结构
    //{
    //  "id":author_id,
    //  "name":author_name,
    //  "plugin":[
    //      {
    //          "plugin_id":plugin_id,
    //          "plugin_name":plugin_name,
    //          "version":{
    //              version:下载地址
    //          }
    //      }
    //      ...
    //
    //   ]
    //
    //}
    @PostMapping("/getAuthorPlugin")
    public ResponseEntity<?> handleGetAuthorPlugin(MultipartHttpServletRequest request) throws Exception {
        SqlSession sqlSession = GetSqlsession.getsqlsession();
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });
            if(!formData.containsKey("author_name")){
                sqlSession.close();
                return ResponseEntity.ok("Fail");
            }
            String author_name=formData.get("author_name");
            AuthorMapper authorMapper=sqlSession.getMapper(AuthorMapper.class);
            Author author=authorMapper.selectByNameUnique(author_name);
            if(author==null){
                sqlSession.close();
                return ResponseEntity.ok("Fail");
            }
            //结果字典
            Map<String,Object> result_map=new HashMap<String,Object>();
            result_map.put("id",author.getAuthor_id());
            result_map.put("name",author.getName());
            PluginMapper pluginMapper=sqlSession.getMapper(PluginMapper.class);
            VersionMapper versionMapper=sqlSession.getMapper(VersionMapper.class);
            //检索插件
            List<Plugin> pluginList=pluginMapper.selectByAuthorID(author.getAuthor_id());
            Map[] cache_array=new Map[pluginList.size()];
            for(int i=0;i<pluginList.size();i++){
                Map cache=new HashMap<>();
                Plugin p= pluginList.get(i);
                cache.put("plugin_id",p.getPlugin_id());
                cache.put("plugin_name",p.getName());
                cache.put("introduction",p.getIntroduction());
                cache.put("author_id",p.getAuthor_id());
                List<Version> versionList=versionMapper.selectByPluginID(p.getPlugin_id());
                Map version_cache=new HashMap();
                //写入id和路径
                for(int j=0;j<versionList.size();j++){
                    Version version=versionList.get(j);
                    Map new_map=new HashMap();
                    new_map.put("path",version.getPath());
                    new_map.put("package_name",version.getPackage_name());
                    version_cache.put(version.getVersion(),new_map);

                }
                cache.put("version",version_cache);
                cache_array[i]=cache;
            }
            result_map.put("plugin",cache_array);
            Gson gson=new Gson();
            System.out.println(gson.toJson(result_map));
            return  ResponseEntity.ok(gson.toJson(result_map));
        } catch (Exception e) {
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }


    //获取一个作者的插件和节点集
    @PostMapping("/getAuthorPluginAndNodeset")
    public ResponseEntity<?> handleGetAuthorPluginAndNodeSet(MultipartHttpServletRequest request) throws Exception {
        SqlSession sqlSession = GetSqlsession.getsqlsession();
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });
            if(!formData.containsKey("author_name")){
                sqlSession.close();
                return ResponseEntity.ok("Fail");
            }
            String author_name=formData.get("author_name");
            AuthorMapper authorMapper=sqlSession.getMapper(AuthorMapper.class);
            Author author=authorMapper.selectByNameUnique(author_name);
            if(author==null){
                sqlSession.close();
                return ResponseEntity.ok("Fail");
            }
            //结果字典
            Map<String,Object> result_map=new HashMap<String,Object>();
            result_map.put("id",author.getAuthor_id());
            result_map.put("name",author.getName());
            PluginMapper pluginMapper=sqlSession.getMapper(PluginMapper.class);
            VersionMapper versionMapper=sqlSession.getMapper(VersionMapper.class);
            //检索插件
            List<Plugin> pluginList=pluginMapper.selectByAuthorID(author.getAuthor_id());
            Map[] cache_array=new Map[pluginList.size()];
            for(int i=0;i<pluginList.size();i++){
                Map cache=new HashMap<>();
                Plugin p= pluginList.get(i);
                cache.put("plugin_id",p.getPlugin_id());
                cache.put("plugin_name",p.getName());
                cache.put("introduction",p.getIntroduction());
                cache.put("author_id",p.getAuthor_id());
                List<Version> versionList=versionMapper.selectByPluginID(p.getPlugin_id());
                Map version_cache=new HashMap();
                //写入id和路径
                for(int j=0;j<versionList.size();j++){
                    Version version=versionList.get(j);
                    Map new_map=new HashMap();
                    new_map.put("path",version.getPath());
                    new_map.put("package_name",version.getPackage_name());
                    version_cache.put(version.getVersion(),new_map);

                }
                cache.put("version",version_cache);
                cache_array[i]=cache;
            }
            result_map.put("plugin",cache_array);

            NodeSetMapper nodeSetMapper=sqlSession.getMapper(NodeSetMapper.class);
            List<NodeSet> nodeSets=nodeSetMapper.selectByAuthorID(author.getAuthor_id());

            Map[] nodeArray=new Map[nodeSets.size()];
            for(int i=0;i<nodeSets.size();i++){
                Map cache=new HashMap<>();
                NodeSet p= nodeSets.get(i);
                cache.put("nodeset_id",p.getSet_id());
                cache.put("nodeset_name",p.getName());
                cache.put("introduction",p.getIntroduction());
                cache.put("author_id",p.getAuthor_id());
                nodeArray[i]=cache;
            }
            result_map.put("nodeset",nodeArray);
            Gson gson=new Gson();
            System.out.println(gson.toJson(result_map));
            return  ResponseEntity.ok(gson.toJson(result_map));
        } catch (Exception e) {
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }

    //通过名字模糊搜索插件
    @PostMapping("/getPlugin")
    public ResponseEntity<?> handleGetPlugin(MultipartHttpServletRequest request) throws Exception {
        SqlSession sqlSession = GetSqlsession.getsqlsession();
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });
            if(!formData.containsKey("content")){
                sqlSession.close();
                return ResponseEntity.ok("Fail");
            }
            //提取搜索参数
            String content=formData.get("content");
            PluginMapper pluginMapper=sqlSession.getMapper(PluginMapper.class);

            //结果字典
            Map<String,Object> result_map=new HashMap<String,Object>();
            result_map.put("content",content);
            //检索插件
            List<Plugin> pluginList=pluginMapper.selectByName("%"+content+"%");

            AuthorMapper authorMapper=sqlSession.getMapper(AuthorMapper.class);


            Map[] cache_array=new Map[pluginList.size()];
            for(int i=0;i<pluginList.size();i++){
                Map cache=new HashMap<>();
                Plugin p= pluginList.get(i);
                cache.put("plugin_id",p.getPlugin_id());
                cache.put("plugin_name",p.getName());
                Author author=authorMapper.selectByID(p.getAuthor_id());
                cache.put("author_id",author.getAuthor_id());
                cache.put("plugin_author",author.getName());
                cache_array[i]=cache;
            }
            result_map.put("plugin",cache_array);
            Gson gson=new Gson();
            System.out.println(gson.toJson(result_map));
            return  ResponseEntity.ok(gson.toJson(result_map));
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }


    //获取某个插件的详细信息（包括插件名，版本等全部信息）
    @PostMapping("/getPluginMes")
    public ResponseEntity<?> handleGetPluginMes(MultipartHttpServletRequest request) throws Exception {
        SqlSession sqlSession = GetSqlsession.getsqlsession();
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });
            if(!formData.containsKey("plugin_id")){
                sqlSession.close();
                return ResponseEntity.ok("Fail");
            }
            //提取搜索参数
            String plugin_id=formData.get("plugin_id");
            PluginMapper pluginMapper=sqlSession.getMapper(PluginMapper.class);

            //结果字典
            Map<String,Object> result_map=new HashMap<String,Object>();
            result_map.put("plugin_id",plugin_id);
            //检索插件
            Plugin plugin=pluginMapper.selectByID(plugin_id);
            if(plugin==null){
                sqlSession.close();
                return ResponseEntity.ok("Fail");
            }
            result_map.put("plugin_name",plugin.getName());
            result_map.put("author_id",plugin.getAuthor_id());
            result_map.put("introduction",plugin.getIntroduction());
            AuthorMapper authorMapper=sqlSession.getMapper(AuthorMapper.class);
            Author author=authorMapper.selectByID(plugin.getAuthor_id());
            result_map.put("plugin_author",author.getName());
            VersionMapper versionMapper=sqlSession.getMapper(VersionMapper.class);
            List<Version> versionList=versionMapper.selectByPluginID(plugin.getPlugin_id());
            Map version_cache=new HashMap();
            //写入id和路径
            for(int j=0;j<versionList.size();j++){
                Version version=versionList.get(j);
                Map new_map=new HashMap();
                new_map.put("path",server_address+"/files/download/"+version.getPath());
                new_map.put("package_name",version.getPackage_name());
                version_cache.put(version.getVersion(),new_map);
            }
            result_map.put("version",version_cache);
            Gson gson=new Gson();
            System.out.println(gson.toJson(result_map));
            return  ResponseEntity.ok(gson.toJson(result_map));
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }



    //删除某插件的指定版本
    @PostMapping("/deleteVersion")
    public ResponseEntity<?> handleDeleteVersion(MultipartHttpServletRequest request) {
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
                System.out.println("参数缺少");
                System.out.println(formData.toString());
                return ResponseEntity.ok().body("Fail");
            }
            if((!formData.containsKey("plugin_id")) || (!formData.containsKey("version") ) ){
                sqlSession.close();
                System.out.println(formData.toString());
                return ResponseEntity.ok().body("Fail");
            }
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            String plugin_id=formData.get("plugin_id");
            String plugin_version=formData.get("version");
            //获取包名
            String package_name=formData.get("package_name");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                System.out.println("密码验证不通过");
                return ResponseEntity.ok().body("Fail");
            }
            VersionMapper versionMapper=sqlSession.getMapper(VersionMapper.class);
            Version version=versionMapper.selectByIDAndVersion(plugin_id,plugin_version);

            PluginMapper pluginMapper=sqlSession.getMapper(PluginMapper.class);
            Plugin plugin=pluginMapper.selectByID(plugin_id);
            Author author=authorMapper.selectByNameUnique(author_name);
            //如果不存在plugin或者plugin不属于该作者，返回失败
            if(plugin==null || !plugin.getAuthor_id().equals(author.getAuthor_id())){
                sqlSession.close();
                System.out.println("不属于该作者");
                System.out.println(plugin.getAuthor_id());
                return ResponseEntity.ok().body("Fail");
            }


            //结果数组
            Map<String,String> result_map=new HashMap<String,String>();
            if(version!=null){
                String path=this.path+"\\"+version.getPath();
                FileUtil.delete(path);
               versionMapper.deleteVersion(plugin_id,plugin_version);
            }
            //不存在
            else{
                sqlSession.close();
                return ResponseEntity.ok("Fail");
            }
            result_map.put("author_id",author.getAuthor_id());
            result_map.put("plugin_id",plugin_id);
            result_map.put("version",plugin_version);

            sqlSession.close();
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }

    //更新插件信息
    @PostMapping("/updatePlugin")
    public ResponseEntity<?> handleUpdatePlugin(MultipartHttpServletRequest request) {
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
                System.out.println("参数缺少");
                System.out.println(formData.toString());
                return ResponseEntity.ok().body("Fail");
            }
            //缺少需要更改的插件ID
            if((!formData.containsKey("plugin_id")) ){
                sqlSession.close();
                System.out.println(formData.toString());
                return ResponseEntity.ok().body("Fail");
            }
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            String plugin_id=formData.get("plugin_id");
            //获取包名
            String package_name=formData.get("package_name");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                System.out.println("密码验证不通过");
                return ResponseEntity.ok().body("Fail");
            }

            Author author=authorMapper.selectByNameUnique(author_name);
            PluginMapper pluginMapper=sqlSession.getMapper(PluginMapper.class);
            Plugin plugin=pluginMapper.selectByID(plugin_id);
            //如果不存在plugin或者plugin不属于该作者，返回失败
            if(plugin==null || !plugin.getAuthor_id().equals(author.getAuthor_id())){
                sqlSession.close();
                System.out.println("不属于该作者");
                System.out.println(plugin.getAuthor_id());
                System.out.println(author_name);
                return ResponseEntity.ok().body("Fail");
            }

            String plugin_name=plugin.getName();
            String plugin_introduction=plugin.getIntroduction();
            //提取更改
            if(formData.containsKey("plugin_name")){
                plugin_name=formData.get("plugin_name");
            }
            if (formData.containsKey("plugin_introduction")){
                plugin_introduction=formData.get("plugin_introduction");
            }
            //数据库操作
            pluginMapper.updatePlugin(plugin_id,plugin_name,plugin_introduction);
            //结果数组
            Map<String,String> result_map=new HashMap<String,String>();
            result_map.put("author_name",author_name);
            result_map.put("plugin_id",plugin_id);
            result_map.put("plugin_name",plugin_name);
            result_map.put("plugin_introduction",plugin_introduction);
            sqlSession.close();
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.close();
            return ResponseEntity.ok("Fail");
        }
    }


    //更新插件信息
    @PostMapping("/deletePlugin")
    public ResponseEntity<?> handleDeletePlugin(MultipartHttpServletRequest request) {
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
                System.out.println("参数缺少");
                System.out.println(formData.toString());
                return ResponseEntity.ok().body("Fail");
            }
            //缺少需要更改的插件ID
            if((!formData.containsKey("plugin_id")) ){
                sqlSession.close();
                System.out.println(formData.toString());
                return ResponseEntity.ok().body("Fail");
            }
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            String plugin_id=formData.get("plugin_id");
            //获取包名
            String package_name=formData.get("package_name");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                System.out.println("密码验证不通过");
                return ResponseEntity.ok().body("Fail");
            }

            Author author=authorMapper.selectByNameUnique(author_name);
            PluginMapper pluginMapper=sqlSession.getMapper(PluginMapper.class);
            Plugin plugin=pluginMapper.selectByID(plugin_id);
            //如果不存在plugin或者plugin不属于该作者，返回失败
            if(plugin==null || !plugin.getAuthor_id().equals(author.getAuthor_id())){
                sqlSession.close();
                System.out.println("不属于该作者");
                System.out.println(author_name);
                return ResponseEntity.ok().body("Fail");
            }

            String plugin_name=plugin.getName();
            String plugin_introduction=plugin.getIntroduction();
            //提取更改


            VersionMapper versionMapper=sqlSession.getMapper(VersionMapper.class);

            List<Version> versionList=versionMapper.selectByPluginID(plugin_id);

            for(Version v:versionList){
                String path=this.path+"\\"+v.getPath();
                FileUtil.delete(path);
                versionMapper.deleteVersion(plugin_id,v.getVersion());

            }

            //删除插件表单记录
            pluginMapper.deletePlugin(plugin_id);


            //结果数组
            Map<String,String> result_map=new HashMap<String,String>();
            result_map.put("author_name",author_name);
            result_map.put("plugin_id",plugin_id);
            result_map.put("plugin_name",plugin_name);
            result_map.put("plugin_introduction",plugin_introduction);
            sqlSession.close();
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.close();
            return ResponseEntity.ok("Fail");
        }
    }







}
