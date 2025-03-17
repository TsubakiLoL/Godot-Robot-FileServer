package tsubaki.database.mapper;

import tsubaki.database.entity.Author;

import java.util.List;

public interface AuthorMapper {
    //查找全部
    List<Author> selectAll();

    //通过ID查找作者
    Author selectByID(String id);

    //通过昵称查找作者
    List<Author> selectByName(String name);

    //判定作者密码是否通过
    Author isAuthorPass(String author_id,String password);

    void addAuthor(String author_id,String name,String password);


    void updateAuthorPassword(String author_id,String new_password);

    void updateAuthorName(String author_id,String new_name);

}
