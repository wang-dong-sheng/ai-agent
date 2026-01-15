//package com.dswang.aiagent.service;
//
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.dswang.aiagent.domain.HisMassage;
//
//import java.util.List;
//
///**
//* @author Administrator
//* @description 针对表【his_massage(历史对话信息)】的数据库操作Service
//* @createDate 2026-01-07 10:25:28
//*/
//public interface HisMassageService extends IService<HisMassage> {
//
//    /**
//     * 获取指定会话的最新N条消息
//     * @param conversationId 会话ID
//     * @param limit 限制数量
//     * @return 消息列表
//     */
//    List<HisMassage> getLatestMessages(String conversationId, int limit);
//
//    /**
//     * 根据会话ID清除消息
//     * @param conversationId 会话ID
//     */
//    void clearByConversationId(String conversationId);
//}