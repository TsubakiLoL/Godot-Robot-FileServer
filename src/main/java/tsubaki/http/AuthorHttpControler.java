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

            if (!formData.containsKey("author_name") || !formData.containsKey("password") ) {
                System.out.println("请求参数不全");
                System.out.println(formData.toString());
                return ResponseEntity.ok("Fail");
            }
            String author_name = formData.get("author_name");
            String password = formData.get("password");

            SqlSession sqlSession = GetSqlsession.getsqlsession();
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);


            if (!Author.isAuthorPass(author_name,password,authorMapper)) {
                sqlSession.close();
                return ResponseEntity.ok("Fail");
            }
            Author author = authorMapper.selectByNameUnique(author_name);
            sqlSession.close();
            return ResponseEntity.ok(Map.of("id",author.getAuthor_id(),"name",author.getName(),"head",author.getHead()));
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

            if (!formData.containsKey("name") || !formData.containsKey("password") ||!formData.containsKey("head")) {
                System.out.println("请求参数不全");
                return ResponseEntity.ok("Fail");
            }
            String name = formData.get("name");
            String password = formData.get("password");
            String head=formData.get("head");

            SqlSession sqlSession = GetSqlsession.getsqlsession();
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            Author author = authorMapper.selectByNameUnique(name);
            if(author!=null){
                System.out.println("存在重名");
                return ResponseEntity.ok("Fail");
            }
            String author_id=signUpAuthor(name,password,head);
            return ResponseEntity.ok(Map.of("id",author_id,"name",name,"password",password,"head",head));
        } catch (Exception e) {
            System.out.println("抛出错误"+e.toString());
            return ResponseEntity.ok("Fail");

        }
    }




    //注册用户返回ID
    String signUpAuthor(String name,String password,String head){
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
        authorMapper.addAuthor(uuid.toString(),name,password,head);
        System.out.println(authorMapper.selectAll().toString());
        sqlSession.commit();
        sqlSession.close();
        return uuid.toString();
    }




    //更新用户信息
    @PostMapping("/update")
    public ResponseEntity<?> handleUpdateAuthor(MultipartHttpServletRequest request) {
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
            Author author = authorMapper.selectByNameUnique(author_name);
            Map<String,String> result_map=new HashMap<String,String>();
            result_map.put("author_name",author_name);
            //如果要更新名字
            if(formData.containsKey("name") ){
                String name = formData.get("name");
                authorMapper.updateAuthorName(author.getAuthor_id(),name);
                result_map.put("name",name);

            }
            if(formData.containsKey("new_password")){
                String new_password=formData.get("new_password");
                authorMapper.updateAuthorPassword(author.getAuthor_id(),new_password);
                result_map.put("password",new_password);
            }

            if(formData.containsKey("head")){
                String head=formData.get("head");
                authorMapper.updateAuthorHead(author.getAuthor_id(),head);
                result_map.put("head",head);
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


