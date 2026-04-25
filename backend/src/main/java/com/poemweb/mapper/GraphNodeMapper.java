package com.poemweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poemweb.entity.GraphNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface GraphNodeMapper extends BaseMapper<GraphNode> {
    
    @Select("SELECT gn.* FROM graph_node gn WHERE gn.name LIKE CONCAT('%', #{keyword}, '%')")
    List<GraphNode> searchByName(@Param("keyword") String keyword);
    
    @Select("SELECT gn.* FROM graph_node gn WHERE gn.category = #{category}")
    List<GraphNode> findByCategory(@Param("category") String category);
}