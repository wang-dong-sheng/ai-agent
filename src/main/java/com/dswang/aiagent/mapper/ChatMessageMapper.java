package com.dswang.aiagent.mapper;

import com.dswang.aiagent.domain.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * chat_messages 表的 MyBatis Mapper
 *
 * @author Mr.Wang
 * @date 2026-01-23
 */
public interface ChatMessageMapper {

    int insert(ChatMessage message);

    int insertBatch(@Param("messages") List<ChatMessage> messages);

    List<ChatMessage> selectAllByConversationId(@Param("conversationId") String conversationId);

    List<ChatMessage> selectAllByUserIdAndConversationId(@Param("userId") Long userId,
                                                         @Param("conversationId") String conversationId);

    List<ChatMessage> selectLatestByConversationId(@Param("conversationId") String conversationId,
                                                   @Param("limit") int limit);

    List<String> selectConversationsByUserId(@Param("userId") Long userId);

    long countByConversationId(@Param("conversationId") String conversationId);

    int deleteByConversationId(@Param("conversationId") String conversationId);
}

