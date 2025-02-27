package tsubaki.database.mapper;

import tsubaki.database.entity.NodeSet;
import tsubaki.database.entity.Plugin;

import java.util.List;

public interface PluginMapper {
    List<Plugin> selectAll();


    List<Plugin> selectByAuthorID(String id);

    Plugin selectByID(String id);

    List<Plugin> selectByName(String name);
}
