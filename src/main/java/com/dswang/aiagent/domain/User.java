package com.dswang.aiagent.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表 users
 *
 * @author Mr.Wang
 * @date 2026-02-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * 用户ID，主键
     */
    private Long id;

    /**
     * 用户名，唯一
     */
    private String username;

    /**
     * 密码哈希值，使用BCrypt加密
     */
    private String passwordHash;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}