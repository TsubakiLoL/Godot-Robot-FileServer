package tsubaki.database.mapper;

import tsubaki.database.entity.NodeSet;

import java.util.List;

public interface NodeSetMapper {
    List<NodeSet> selectAll();


    List<NodeSet> selectByAuthorID(String id);

    List<NodeSet> selectByName(String name);
}
