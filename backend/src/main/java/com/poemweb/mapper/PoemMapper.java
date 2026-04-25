package com.poemweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poemweb.entity.Poem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PoemMapper extends BaseMapper<Poem> {
    // 根据关键词、作者、朝代、题材查询诗词
    List<Poem> searchPoems(
            @Param("keyword") String keyword,
            @Param("author") String author,
            @Param("dynasty") String dynasty,
            @Param("tag") String tag
    );
    
    // 根据标题查询诗词详情
    Poem getPoemByTitle(@Param("title") String title);
    
    // 获取所有诗词（用于随机推荐）
    List<Poem> getAllPoems();
    
    // 根据作者ID获取诗词
    List<Poem> getPoemsByAuthorId(@Param("authorId") Long authorId);
    
    // 根据朝代ID获取诗词
    List<Poem> getPoemsByDynastyId(@Param("dynastyId") Long dynastyId);
    
    // 根据题材ID获取诗词
    List<Poem> getPoemsByTagId(@Param("tagId") Long tagId);
}