package tsubaki.http;


import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartHttpServletRequest;
import tsubaki.database.entity.Author;
import tsubaki.database.mapper.AuthorMapper;
import tsubaki.database.mybatis.GetSqlsession;
import tsubaki.util.MD5Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/author")
public class AuthorHttpControler {
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
        while(authorMapper.selectByID(uuid.toString())!=null && times<=max_search_times) {
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




    //更新用户信息
    @PostMapping("/updateAuthor")
    public ResponseEntity<?> handleUpdateAuthor(MultipartHttpServletRequest request) {
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




            String author_id=formData.get("author_id");
            String password=formData.get("password");
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            //如果密码验证不通过
            if(! Author.isAuthorPass(author_id,password,authorMapper)){
                sqlSession.close();
                return ResponseEntity.ok().body("Fail");
            }
            Map<String,String> result_map=new HashMap<String,String>();
            result_map.put("author_id",author_id);
            //如果要更新名字
            if(formData.containsKey("name") ){
                String name = formData.get("name");
                authorMapper.updateAuthorName(author_id,name);
                result_map.put("name",name);

            }
            if(formData.containsKey("new_password")){
                String new_password=formData.get("new_pasword");
                authorMapper.updateAuthorPassword(author_id,new_password);
                result_map.put("password",new_password);
            }

            sqlSession.close();
            return ResponseEntity.ok(result_map);
        } catch (Exception e) {
            System.out.println("抛出错误"+e.toString());
            sqlSession.close();
            return ResponseEntity.ok("Fail");

        }
    }



}


