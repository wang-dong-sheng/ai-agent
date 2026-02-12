package com.dswang.aiagent.service.impl;

import com.dswang.aiagent.domain.User;
import com.dswang.aiagent.mapper.UserMapper;
import com.dswang.aiagent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 *
 * @author Mr.Wang
 * @date 2026-02-12
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public User register(String username, String password) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 加密密码
        String passwordHash = passwordEncoder.encode(password);

        // 创建用户
        User user = User.builder()
                .username(username)
                .passwordHash(passwordHash)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 保存用户
        int result = userMapper.insert(user);
        if (result != 1) {
            throw new RuntimeException("注册失败");
        }

        return user;
    }

    @Override
    public User login(String username, String password) {
        // 根据用户名查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        return user;
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        int result = userMapper.update(user);
        if (result != 1) {
            throw new RuntimeException("更新失败");
        }
        return userMapper.selectById(user.getId());
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        int result = userMapper.delete(id);
        return result == 1;
    }
}