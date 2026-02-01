-- 聊天消息表
-- 用于存储历史聊天记录

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引以优化查询性能
-- 1. 对话ID索引：快速定位某个对话的所有消息
CREATE INDEX IF NOT EXISTS idx_chat_messages_conversation_id ON chat_messages(conversation_id);

-- 2. 复合索引：支持按对话ID和时间排序查询（最常用场景）
CREATE INDEX IF NOT EXISTS idx_chat_messages_conversation_created ON chat_messages(conversation_id, created_at DESC);

-- 3. 时间索引：支持按时间范围查询
CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at DESC);

-- 添加表注释
COMMENT ON TABLE chat_messages IS '聊天消息表，存储历史对话记录';

-- 添加列注释（PostgreSQL使用COMMENT ON COLUMN）
COMMENT ON COLUMN chat_messages.id IS '主键ID';
COMMENT ON COLUMN chat_messages.conversation_id IS '对话组ID，用于关联同一对话的所有消息';
COMMENT ON COLUMN chat_messages.message_type IS '消息类型：USER(用户消息), ASSISTANT(助手消息), SYSTEM(系统消息)';
COMMENT ON COLUMN chat_messages.content IS '消息文本内容';
COMMENT ON COLUMN chat_messages.metadata IS '消息元数据，JSON格式，可存储消息的额外信息';
COMMENT ON COLUMN chat_messages.created_at IS '消息创建时间';
COMMENT ON COLUMN chat_messages.updated_at IS '消息更新时间';
