package com.dswang.aiagent.controller;

import com.dswang.aiagent.domain.ChatMessage;
import com.dswang.aiagent.mapper.ChatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话历史控制器
 * 提供查询用户会话列表和会话消息详情的接口
 *
 * @author Mr.Wang
 * @date 2026-02-12
 */
@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    /**
     * 根据用户ID查询会话列表
     */
    @GetMapping("/list")
    public Map<String, Object> getConversationList(@RequestParam("userId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> conversationIds = chatMessageMapper.selectConversationsByUserId(userId);
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", conversationIds);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * 根据用户ID和会话ID查询消息详情
     */
    @GetMapping("/messages")
    public Map<String, Object> getConversationMessages(@RequestParam("userId") Long userId, 
                                                     @RequestParam("conversationId") String conversationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ChatMessage> messages = chatMessageMapper.selectAllByUserIdAndConversationId(userId, conversationId);
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", messages);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}