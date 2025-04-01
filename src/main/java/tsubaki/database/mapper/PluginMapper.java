package tsubaki.database.mapper;

import tsubaki.database.entity.NodeSet;
import tsubaki.database.entity.Plugin;

import java.util.List;

public interface PluginMapper {
    List<Plugin> selectAll();


    List<Plugin> selectByAuthorID(String author_id);

    Plugin selectByID(String plugin_id);

    List<Plugin> selectByName(String name);



    void insertPlugin(String plugin_id,String name,String author_id,String introduction);



    void updatePlugin(String plugin_id,String name,String introduction);


    void deletePlugin(String plugin_id);
}
