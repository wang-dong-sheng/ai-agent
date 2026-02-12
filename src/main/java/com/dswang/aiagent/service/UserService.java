package com.dswang.aiagent.service;

import com.dswang.aiagent.domain.User;

/**
 * 用户服务接口
 *
 * @author Mr.Wang
 * @date 2026-02-12
 */
public interface UserService {

    /**
     * 用户注册
     */
    User register(String username, String password);

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 根据ID查询用户
     */
    User getUserById(Long id);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);

    /**
     * 更新用户信息
     */
    User updateUser(User user);

    /**
     * 删除用户
     */
    boolean deleteUser(Long id);
}