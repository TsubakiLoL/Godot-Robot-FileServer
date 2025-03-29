package tsubaki.database.mapper;

import tsubaki.database.entity.Version;

import java.util.List;

public interface  VersionMapper {

    Version selectByIDAndVersion(String plugin_id, String version);

    List<Version>  selectByPluginID(String plugin_id);


    void insertVersion(String plugin_id,String version,String path,String package_name);


    void deleteVersion(String plugin_id,String version);
}
