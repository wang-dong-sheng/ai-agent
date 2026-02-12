package com.dswang.aiagent.controller;

import com.dswang.aiagent.domain.User;
import com.dswang.aiagent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 提供用户注册、登录等接口
 *
 * @author Mr.Wang
 * @date 2026-02-12
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = params.get("username");
            String password = params.get("password");
            if (username == null || password == null) {
                throw new RuntimeException("用户名和密码不能为空");
            }
            User user = userService.register(username, password);
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("data", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername()
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = params.get("username");
            String password = params.get("password");
            if (username == null || password == null) {
                throw new RuntimeException("用户名和密码不能为空");
            }
            User user = userService.login(username, password);
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("data", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername()
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestParam("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                response.put("success", true);
                response.put("data", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "createdAt", user.getCreatedAt()
                ));
            } else {
                response.put("success", false);
                response.put("message", "用户不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Map<String, Object> updateUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            User updatedUser = userService.updateUser(user);
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", Map.of(
                    "id", updatedUser.getId(),
                    "username", updatedUser.getUsername()
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete")
    public Map<String, Object> deleteUser(@RequestParam("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = userService.deleteUser(id);
            if (result) {
                response.put("success", true);
                response.put("message", "删除成功");
            } else {
                response.put("success", false);
                response.put("message", "删除失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}