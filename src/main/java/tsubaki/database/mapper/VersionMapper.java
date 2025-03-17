package tsubaki.database.mapper;

import tsubaki.database.entity.Version;

public interface  VersionMapper {

    Version selectByIDAndVersion(String plugin_id, String version);


    void insertVersion(String plugin_id,String version,String path);
}
