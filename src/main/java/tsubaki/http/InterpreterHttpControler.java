package tsubaki.http;


import com.google.gson.Gson;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tsubaki.database.entity.Author;
import tsubaki.database.mapper.AuthorMapper;
import tsubaki.database.mybatis.GetSqlsession;
import tsubaki.interpreter.InterpreterDB;
import tsubaki.util.ZIPUtil;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

//用于管理云端实例的控制器
@RestController
@RequestMapping("/interpreter")
public class InterpreterHttpControler {

    //上传打包到指定文件
    @PostMapping("/upload")
    public ResponseEntity<?> handleUpload(MultipartHttpServletRequest request) {
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
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }

            Author author=authorMapper.selectByNameUnique(author_name);
            Map<String,String> result_map=new HashMap<String,String>();
            result_map.put("author_name",author_name);
;

            //强转类型，获取输入文件流
            InputStream fileInputStream=((MultipartFile)(files.values().toArray()[0])).getInputStream();
            if(InterpreterDB.get().hasProcess(author.getAuthor_id())){
                InterpreterDB.get().deleteProcess(author.getAuthor_id());
            }
            ZIPUtil.unzipStreamToDirectory(fileInputStream, Path.of(InterpreterDB.getUserSandBoxPath(author.getAuthor_id())));

            sqlSession.close();
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }

    @PostMapping("/start")
    public ResponseEntity<?> handleStart(MultipartHttpServletRequest request) {
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
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            Map<String,String> result_map=new HashMap<String,String>();
            Author author=authorMapper.selectByNameUnique(author_name);
            result_map.put("author_name",author_name);
            if(InterpreterDB.get().hasProcess(author.getAuthor_id())){
                InterpreterDB.get().deleteProcess(author.getAuthor_id());
            }
            sqlSession.close();
            InterpreterDB.get().createProcess(author.getAuthor_id());
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }


    @PostMapping("/stop")
    public ResponseEntity<?> handleStop(MultipartHttpServletRequest request) {
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
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            Author author=authorMapper.selectByNameUnique(author_name);
            if(InterpreterDB.get().hasProcess(author.getAuthor_id())){
                InterpreterDB.get().deleteProcess(author.getAuthor_id());
            }
            Map<String,String> result_map=new HashMap<String,String>();
            result_map.put("author_name",author_name);


            sqlSession.close();
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }


    @PostMapping("/running")
    public ResponseEntity<?> handleRunning(MultipartHttpServletRequest request) {
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
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            Map<String, Boolean> result_map=new HashMap<String, Boolean>();

            Author author=authorMapper.selectByNameUnique(author_name);
            if(InterpreterDB.get().hasProcess(author.getAuthor_id())){
                result_map.put("running",true);
            }
            else{
                result_map.put("running",false);
            }


            Gson gson=new Gson();
            System.out.println(gson.toJson(result_map));
            sqlSession.close();
            return  ResponseEntity.ok(gson.toJson(result_map));
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }

    @PostMapping("/blog")
    public ResponseEntity<?> handleBlog(MultipartHttpServletRequest request) {
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
            String author_name=formData.get("author_name");
            String password=formData.get("password");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_name,password,authorMapper)){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            Map<String, java.util.List<String>> result_map=new HashMap<>();

            Author author=authorMapper.selectByNameUnique(author_name);
            result_map.put("data",InterpreterDB.get().getProcessBlog(author.getAuthor_id()));


            Gson gson=new Gson();
            System.out.println(gson.toJson(result_map));
            sqlSession.close();
            return  ResponseEntity.ok(gson.toJson(result_map));
        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }


    @PostMapping("/input")
    public ResponseEntity<?> handleInput(MultipartHttpServletRequest request) {
        SqlSession sqlSession = GetSqlsession.getsqlsession();
        try {
            // 获取普通参数
            Map<String, String> formData = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                formData.put(key, values.length > 0 ? values[0] : null);
            });

            // 参数检查
            if (!formData.containsKey("author_name") ||
                    !formData.containsKey("password") ||
                    !formData.containsKey("input")) {
                return ResponseEntity.ok().body("Failed");
            }

            // 身份验证
            String authorName = formData.get("author_name");
            String password = formData.get("password");
            String input=formData.get("input");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            if (!Author.isAuthorPass(authorName, password, authorMapper)) {
                return ResponseEntity.ok().body("Failed");
            }

            Map<String, String> result_map=new HashMap<>();
            result_map.put("author_name",authorName);
            result_map.put("input",input);

            Author author = authorMapper.selectByNameUnique(authorName);
            String authorId = author.getAuthor_id();

            InterpreterDB.get().input(authorId,input);
            return ResponseEntity.ok(new Gson().toJson(result_map));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Failed");
        } finally {
            sqlSession.close();
        }
    }



}
