package com.poemweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poemweb.entity.PoetTrajectory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PoetTrajectoryMapper extends BaseMapper<PoetTrajectory> {
    @Select("SELECT DISTINCT poet_name FROM poet_trajectory ORDER BY poet_name")
    List<String> selectDistinctPoetNames();
    
    @Select("SELECT * FROM poet_trajectory WHERE poet_name = #{poetName} ORDER BY year ASC")
    List<PoetTrajectory> selectByPoetNameOrderByYearAsc(@Param("poetName") String poetName);
    
    @Select("SELECT * FROM poet_trajectory ORDER BY poet_name ASC, year ASC")
    List<PoetTrajectory> selectAllOrderByPoetNameYearAsc();
}
