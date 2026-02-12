package com.dswang.aiagent.mapper;

import com.dswang.aiagent.domain.User;
import org.apache.ibatis.annotations.Param;

/**
 * users 表的 MyBatis Mapper
 *
 * @author Mr.Wang
 * @date 2026-02-12
 */
public interface UserMapper {

    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户信息
     */
    int update(User user);

    /**
     * 删除用户
     */
    int delete(@Param("id") Long id);
}