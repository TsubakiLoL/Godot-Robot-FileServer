package tsubaki.database.mapper;

import tsubaki.database.entity.NodeSet;

import java.util.List;

public interface NodeSetMapper {
    List<NodeSet> selectAll();


    List<NodeSet> selectByAuthorID(String id);

    List<NodeSet> selectByName(String name);

    void addNodeSet( String set_id, String author_id, String introduction, String name, String path);


    void deleteNodeSetBySetID(String set_id);


    void deleteNodeSetByAuthorID(String author_id);




}
