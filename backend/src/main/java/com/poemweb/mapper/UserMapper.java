package com.poemweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poemweb.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
    
    @Select("SELECT * FROM sys_user WHERE email = #{email}")
    User findByEmail(@Param("email") String email);
    
    @Update("UPDATE sys_user SET last_login_time = NOW() WHERE id = #{id}")
    void updateLastLoginTime(@Param("id") Long id);
}
