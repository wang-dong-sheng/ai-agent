package com.dswang.aiagent.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 * 对应数据库表 chat_messages
 *
 * @author Mr.Wang
 * @date 2026-01-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 对话组ID
     */
    private String conversationId;

    /**
     * 消息类型：USER, ASSISTANT, SYSTEM
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息元数据（JSON格式）
     */
    private String metadata;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
