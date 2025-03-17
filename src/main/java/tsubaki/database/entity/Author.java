package tsubaki.database.entity;

import org.apache.ibatis.session.SqlSession;
import org.springframework.http.ResponseEntity;
import tsubaki.database.mapper.AuthorMapper;
import tsubaki.util.MD5Util;

public class Author {
    private String author_id;
    private String name;
    private String password;

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Author{" +
                "author_id='" + author_id + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    //返回author是否密码验证通过

    public static boolean isAuthorPass(String author_id, String password, AuthorMapper authorMapper){
        Author author = authorMapper.selectByID(author_id);
        if (author == null) {

            return false;

        }
        String MD5 = MD5Util.generateMD5(author.getPassword());

        if (!(MD5.equals( password))) {
            return false;
        }
        return true;
    }
}
