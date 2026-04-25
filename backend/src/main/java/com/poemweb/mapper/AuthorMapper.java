package com.poemweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poemweb.entity.Author;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthorMapper extends BaseMapper<Author> {
    // 可以添加自定义方法
}