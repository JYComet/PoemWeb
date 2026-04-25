package com.poemweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poemweb.entity.GraphEdge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface GraphEdgeMapper extends BaseMapper<GraphEdge> {
    
    @Select("SELECT ge.* FROM graph_edge ge WHERE ge.source_id = #{sourceId}")
    List<GraphEdge> findBySourceId(@Param("sourceId") Long sourceId);
    
    @Select("SELECT ge.* FROM graph_edge ge WHERE ge.target_id = #{targetId}")
    List<GraphEdge> findByTargetId(@Param("targetId") Long targetId);
    
    @Select("SELECT ge.* FROM graph_edge ge WHERE ge.relation = #{relation}")
    List<GraphEdge> findByRelation(@Param("relation") String relation);
}