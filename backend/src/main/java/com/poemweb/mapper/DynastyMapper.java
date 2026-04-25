package com.poemweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poemweb.entity.Dynasty;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DynastyMapper extends BaseMapper<Dynasty> {
    // 可以添加自定义方法
}