package com.redis.demo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select * from user")
    List<User> getUserList();

    @Select("select * from user where username=#{username}")
    List<User> getUserList2(@Param("username")String username);
}