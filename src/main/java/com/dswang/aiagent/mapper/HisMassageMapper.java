//package com.dswang.aiagent.mapper;
//
//import com.dswang.aiagent.domain.HisMassage;
//import com.baomidou.mybatisplus.core.mapper.BaseMapper;
//import org.apache.ibatis.annotations.Param;
//
//import java.util.List;
//
///**
//* @author Administrator
//* @description 针对表【his_massage(历史对话信息)】的数据库操作Mapper
//* @createDate 2026-01-07 10:25:28
//* @Entity generator.domain.HisMassage
//*/
//public interface HisMassageMapper extends BaseMapper<HisMassage> {
//
//    /**
//     * 获取指定会话的最新N条消息
//     * @param conversationId 会话ID
//     * @param limit 限制数量
//     * @return 消息列表
//     */
//    List<HisMassage> getLatestMessages(@Param("conversationId") String conversationId, @Param("limit") int limit);
//
//    /**
//     * 根据会话ID清除消息
//     * @param conversationId 会话ID
//     * @return 影响的行数
//     */
//    int clearByConversationId(@Param("conversationId") String conversationId);
//}