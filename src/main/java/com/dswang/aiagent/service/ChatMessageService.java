package com.dswang.aiagent.service;

import com.dswang.aiagent.domain.ChatMessage;
import com.dswang.aiagent.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天消息Service
 *
 * @author Mr.Wang
 * @date 2026-01-23
 */
@Service
public class ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    public ChatMessageService(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    /**
     * 保存消息
     */
    public void save(ChatMessage message) {
        if (message == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(now);
        }
        if (message.getUpdatedAt() == null) {
            message.setUpdatedAt(now);
        }
        chatMessageMapper.insert(message);
    }

    /**
     * 批量保存消息
     */
    public void saveAll(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (ChatMessage m : messages) {
            if (m == null) {
                continue;
            }
            if (m.getCreatedAt() == null) {
                m.setCreatedAt(now);
            }
            if (m.getUpdatedAt() == null) {
                m.setUpdatedAt(now);
            }
        }
        // MyBatis 批量 insert（单条 SQL 多 values）
        chatMessageMapper.insertBatch(messages);
    }

    /**
     * 根据对话ID获取所有消息
     */
    public List<ChatMessage> getAllMessages(String conversationId) {
        return chatMessageMapper.selectAllByConversationId(conversationId);
    }

    /**
     * 根据对话ID获取最新的N条消息
     */
    public List<ChatMessage> getLatestMessages(String conversationId, int limit) {
        if (limit <= 0) {
            return new ArrayList<>();
        }
        return chatMessageMapper.selectLatestByConversationId(conversationId, limit);
    }

    /**
     * 根据对话ID删除所有消息
     */
    public void deleteByConversationId(String conversationId) {
        chatMessageMapper.deleteByConversationId(conversationId);
    }

    /**
     * 根据对话ID统计消息数量
     */
    public long countByConversationId(String conversationId) {
        return chatMessageMapper.countByConversationId(conversationId);
    }
}
