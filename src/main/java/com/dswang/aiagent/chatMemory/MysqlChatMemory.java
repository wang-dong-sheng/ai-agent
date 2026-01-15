//package com.dswang.aiagent.chatMemory;
//
//
//import com.dswang.aiagent.domain.HisMassage;
////import com.dswang.aiagent.service.HisMassageService;
//import jakarta.annotation.Resource;
//import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.ai.chat.messages.Message;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 基于 MySQL 持久化的对话记忆
// */
//@Component
//public class MysqlChatMemory implements ChatMemory {
//
//    @Resource
//    private HisMassageService hisMassageService;
//
//    @Override
//    public void add(String conversationId, List<Message> messages) {
//        for (Message message : messages) {
//            HisMassage hisMassage = new HisMassage();
//            hisMassage.setContainid(conversationId);
//            hisMassage.setMassage(message.getText());
//            hisMassageService.save(hisMassage);
//        }
//    }
//
//    @Override
//    public List<Message> get(String conversationId, int lastN) {
//        // 查询指定会话的最后 N 条记录
//        List<HisMassage> hisMassages = hisMassageService.getLatestMessages(conversationId, lastN);
//
//        List<Message> messages = new ArrayList<>();
//        for (HisMassage hisMassage : hisMassages) {
//            // 根据消息类型创建对应的消息对象
//            Message message = new org.springframework.ai.chat.messages.AssistantMessage(hisMassage.getMassage());
//            messages.add(message);
//        }
//        return messages;
//    }
//
//    @Override
//    public void clear(String conversationId) {
//        // 清空指定会话的所有消息
//        hisMassageService.clearByConversationId(conversationId);
//    }
//}
