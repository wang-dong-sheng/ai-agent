package com.dswang.aiagent.chatMemory;

import com.dswang.aiagent.domain.ChatMessage;
import com.dswang.aiagent.service.ChatMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于PostgreSQL持久化的对话记忆
 * 实现ChatMemory接口，将聊天记录存储到PostgreSQL数据库
 *
 * @author Mr.Wang
 * @date 2026-01-23
 */
@Slf4j
@Component
public class PostgresChatMemory implements ChatMemory {

    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;

    public PostgresChatMemory(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        List<ChatMessage> chatMessages = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Message message : messages) {
            ChatMessage chatMessage = convertToChatMessage(conversationId, message, now);
            chatMessages.add(chatMessage);
        }

        try {
            chatMessageService.saveAll(chatMessages);
            log.debug("Saved {} messages for conversation: {}", chatMessages.size(), conversationId);
        } catch (Exception e) {
            log.error("Failed to save messages for conversation: {}", conversationId, e);
            throw new RuntimeException("Failed to save chat messages", e);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        try {
            // 获取最新的N条消息
            List<ChatMessage> chatMessages = chatMessageService.getLatestMessages(conversationId, lastN);
            
            List<Message> messages = new ArrayList<>();
            for (ChatMessage chatMessage : chatMessages) {
                Message message = convertToMessage(chatMessage);
                if (message != null) {
                    messages.add(message);
                }
            }
            
            log.debug("Retrieved {} messages for conversation: {}", messages.size(), conversationId);
            return messages;
        } catch (Exception e) {
            log.error("Failed to retrieve messages for conversation: {}", conversationId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void clear(String conversationId) {
        try {
            chatMessageService.deleteByConversationId(conversationId);
            log.debug("Cleared messages for conversation: {}", conversationId);
        } catch (Exception e) {
            log.error("Failed to clear messages for conversation: {}", conversationId, e);
            throw new RuntimeException("Failed to clear chat messages", e);
        }
    }

    /**
     * 将Spring AI的Message转换为ChatMessage实体
     */
    private ChatMessage convertToChatMessage(String conversationId, Message message, LocalDateTime timestamp) {
        String messageType = getMessageType(message);
        String content = message.getText() != null ? message.getText() : "";
        
        // 将消息的元数据转换为JSON字符串
        String metadata = null;
        try {
            Map<String, Object> metadataMap = new HashMap<>();
            if (message.getMetadata() != null) {
                metadataMap.putAll(message.getMetadata());
            }
            if (!metadataMap.isEmpty()) {
                metadata = objectMapper.writeValueAsString(metadataMap);
            }
        } catch (Exception e) {
            log.warn("Failed to serialize message metadata", e);
        }

        return ChatMessage.builder()
                .conversationId(conversationId)
                .messageType(messageType)
                .content(content)
                .metadata(metadata)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }

    /**
     * 将ChatMessage实体转换为Spring AI的Message
     */
    private Message convertToMessage(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return null;
        }
        
        String content = chatMessage.getContent();
        if (content == null) {
            content = "";
        }

        // 解析元数据（注意：当前 Spring AI 版本的 UserMessage/SystemMessage/AssistantMessage
        // 构造器不支持 (String, Map) 形式，因此这里只做解析但不回填到 Message 对象中，
        // 仍然保留在数据库中，后续如升级 Spring AI 可再启用回填。）
        if (chatMessage.getMetadata() != null && !chatMessage.getMetadata().trim().isEmpty()) {
            try {
                objectMapper.readValue(chatMessage.getMetadata(), Map.class);
            } catch (Exception e) {
                log.warn("Failed to deserialize message metadata for message id: {}", chatMessage.getId(), e);
            }
        }

        String messageType = chatMessage.getMessageType();
        if (messageType == null) {
            log.warn("Message type is null for message id: {}, treating as USER message", chatMessage.getId());
            messageType = "USER";
        }

        return switch (messageType.toUpperCase()) {
            case "USER" -> new UserMessage(content);
            case "ASSISTANT" -> new AssistantMessage(content);
            case "SYSTEM" -> new SystemMessage(content);
            default -> {
                log.warn("Unknown message type: {}, treating as USER message for message id: {}", 
                        messageType, chatMessage.getId());
                yield new UserMessage(content);
            }
        };
    }

    /**
     * 获取消息类型字符串
     */
    private String getMessageType(Message message) {
        if (message instanceof UserMessage) {
            return "USER";
        } else if (message instanceof AssistantMessage) {
            return "ASSISTANT";
        } else if (message instanceof SystemMessage) {
            return "SYSTEM";
        } else {
            // 通过类名判断
            String className = message.getClass().getSimpleName();
            if (className.contains("User")) {
                return "USER";
            } else if (className.contains("Assistant")) {
                return "ASSISTANT";
            } else if (className.contains("System")) {
                return "SYSTEM";
            }
            return "USER"; // 默认类型
        }
    }
}
