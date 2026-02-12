<template>
  <div class="chat-view" :class="{ 'dark-mode': isDarkMode }">
    <!-- 会话标题栏 -->
    <div class="chat-header">
      <div class="chat-title">
        <h2>{{ currentConversationTitle }}</h2>
        <el-button 
          type="text" 
          size="small" 
          class="rename-button"
          @click="showRenameDialog = true"
        >
          <el-icon><Edit /></el-icon>
        </el-button>
      </div>
    </div>
    
    <!-- 消息列表 -->
    <div class="message-list" ref="messageListRef">
      <!-- 空对话引导 -->
      <div v-if="messages.length === 0" class="empty-chat">
        <el-icon class="empty-icon"><ChatDotRound /></el-icon>
        <h3>您好！我是AI助手</h3>
        <p>有什么可以帮您？</p>
      </div>
      
      <!-- 消息气泡 -->
      <div 
        v-for="(message, index) in messages" 
        :key="message.id || index"
        class="message-item"
        :class="{ 'user-message': message.role === 'user', 'assistant-message': message.role === 'assistant' }"
      >
        <!-- 头像 -->
        <div class="message-avatar">
          <img v-if="message.role === 'user' && user.avatar" :src="user.avatar" :alt="user.nickname || user.username" />
          <div v-else-if="message.role === 'user'" class="avatar-placeholder">
            {{ getUserInitial(user) }}
          </div>
          <div v-else class="avatar-placeholder assistant-avatar">
            <el-icon><Cpu /></el-icon>
          </div>
        </div>
        
        <!-- 消息内容 -->
        <div class="message-content">
          <div v-if="message.isStreaming" class="message-bubble streaming-bubble">
            <div v-html="formatMessageContent(message.content)"></div>
            <div class="typing-indicator" v-if="message.content === ''">
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
            </div>
          </div>
          <div v-else class="message-bubble" v-html="formatMessageContent(message.content)"></div>
          <div class="message-time">{{ formatTime(message.createdAt) }}</div>
        </div>
      </div>
      
      <!-- 加载更多 -->
      <div v-if="showLoadMore" class="load-more">
        <el-button type="text" @click="loadMoreMessages">加载更多</el-button>
      </div>
    </div>
    
    <!-- 消息输入框 -->
    <div class="message-input-area">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="1"
        :maxlength="2000"
        resize="none"
        placeholder="输入消息..."
        :disabled="!props.conversationId"
        @keyup.enter.exact="sendMessage"
        @keyup.enter.shift="" 
        class="message-input"
      />
      <el-button 
        type="primary" 
        round 
        @click="sendMessage"
        :disabled="!inputMessage.trim() || isSending"
        :loading="isSending"
        class="send-button"
      >
        <el-icon><Message /></el-icon>
      </el-button>
    </div>
    
    <!-- 重命名对话框 -->
    <el-dialog
      v-model="showRenameDialog"
      title="重命名对话"
      width="400px"
    >
      <el-input v-model="newConversationTitle" placeholder="请输入对话标题" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRenameDialog = false">取消</el-button>
          <el-button type="primary" @click="renameConversation">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useConversationStore } from '../store/conversation'
import { useUserStore } from '../store/user'
import { useAppStore } from '../store/app'
import { Edit, ChatDotRound, Cpu, Message } from '@element-plus/icons-vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const props = defineProps({
  conversationId: {
    type: String,
    default: ''
  }
})

const router = useRouter()
const conversationStore = useConversationStore()
const userStore = useUserStore()
const appStore = useAppStore()

// 状态
const inputMessage = ref('')
const isSending = ref(false)
const showRenameDialog = ref(false)
const newConversationTitle = ref('')
const messageListRef = ref(null)
const showLoadMore = ref(false)
const page = ref(1)
const pageSize = ref(20)

// 计算属性
const isDarkMode = computed(() => appStore.darkMode)
const messages = computed(() => conversationStore.currentMessages)
const currentConversation = computed(() => {
  if (!props.conversationId) return null
  return conversationStore.conversations.find(c => c.id === props.conversationId)
})
const currentConversationTitle = computed(() => {
  if (!currentConversation.value) return '新对话'
  if (currentConversation.value.title) return currentConversation.value.title
  if (messages.value.length > 0) {
    const firstMessage = messages.value.find(msg => msg.role === 'user')
    if (firstMessage) {
      return firstMessage.content.substring(0, 30) + (firstMessage.content.length > 30 ? '...' : '')
    }
  }
  return '新对话'
})
const user = computed(() => userStore.user)

// 方法
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !props.conversationId || isSending.value) return
  
  const content = inputMessage.value.trim()
  inputMessage.value = ''
  isSending.value = true
  
  try {
    // 发送消息到后端
    await conversationStore.sendMessage({
      conversationId: props.conversationId,
      content: content
    })
    
  } catch (error) {
    console.error('发送消息失败:', error)
  } finally {
    isSending.value = false
  }
}

// 配置 marked
marked.setOptions({
  breaks: true, // 支持 GitHub 风格的换行
  gfm: true, // 启用 GitHub 风格 Markdown
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  }
})

const formatMessageContent = (content) => {
  if (!content) return ''

  try {
    // 使用 marked 解析 Markdown
    const html = marked.parse(content)
    return html
  } catch (error) {
    console.error('Markdown 解析失败:', error)
    // 降级处理：简单的 HTML 转义
    return content
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\n/g, '<br>')
  }
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const getUserInitial = (user) => {
  if (user.nickname) return user.nickname.charAt(0).toUpperCase()
  if (user.username) return user.username.charAt(0).toUpperCase()
  return 'U'
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const loadMoreMessages = () => {
  // 实现加载更多消息的逻辑
  page.value++
  // 调用store action加载更多消息
  // store.dispatch('conversation/fetchMessages', { conversationId: props.conversationId, page: page.value, pageSize: pageSize.value })
}

const renameConversation = () => {
  if (newConversationTitle.value.trim() && currentConversation.value) {
    // 调用store action重命名对话
    // store.dispatch('conversation/renameConversation', { id: props.conversationId, title: newConversationTitle.value.trim() })
    showRenameDialog.value = false
    newConversationTitle.value = ''
  }
}

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  scrollToBottom()
}, { deep: true })

// 监听会话ID变化，加载对应消息
watch(() => props.conversationId, (newId) => {
  if (newId) {
    conversationStore.fetchMessages(newId)
  }
})

// 生命周期
onMounted(async () => {
  if (props.conversationId) {
    conversationStore.fetchMessages(props.conversationId)
  } else {
    // 自动创建新对话
    const newConversation = await conversationStore.createConversation()
    // 更新路由
    router.push(`/chat/${newConversation.id}`)
  }
  scrollToBottom()
})
</script>

<style lang="scss" scoped>
.chat-view {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  
  &.dark-mode {
    background-color: #1a1a1a;
  }
  
  .chat-header {
    padding: 20px;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .dark-mode & {
      border-bottom-color: #333;
    }
    
    .chat-title {
      display: flex;
      align-items: center;
      
      h2 {
        margin: 0;
        font-size: 18px;
        font-weight: 500;
        color: #303133;
        
        .dark-mode & {
          color: #e0e0e0;
        }
      }
      
      .rename-button {
        margin-left: 12px;
        color: #909399;
        
        &:hover {
          color: #409eff;
        }
      }
    }
  }
  
  .message-list {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    
    .empty-chat {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      
      .empty-icon {
        font-size: 64px;
        color: #c0c4cc;
        margin-bottom: 20px;
        
        .dark-mode & {
          color: #555;
        }
      }
      
      h3 {
        margin: 0 0 8px 0;
        font-size: 20px;
        color: #303133;
        
        .dark-mode & {
          color: #e0e0e0;
        }
      }
      
      p {
        margin: 0;
        color: #909399;
        
        .dark-mode & {
          color: #909090;
        }
      }
    }
    
    .message-item {
      display: flex;
      margin-bottom: 24px;
      gap: 12px;

      &.user-message {
        flex-direction: row;
        justify-content: flex-end;

        .message-avatar {
          order: 2;
          margin-left: 12px;
          margin-right: 0;
        }

        .message-content {
          order: 1;
          align-items: flex-end;
          max-width: calc(100% - 60px);

          .message-bubble {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
            border-radius: 18px 18px 4px 18px;
            box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);

            // 深色模式下的用户消息
            .dark-mode & {
              background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
              box-shadow: 0 2px 8px rgba(139, 92, 246, 0.4);
            }
          }
        }
      }

      &.assistant-message {
        flex-direction: row;
        justify-content: flex-start;

        .message-avatar {
          order: 1;
          margin-right: 12px;
          margin-left: 0;
        }

        .message-content {
          order: 2;
          align-items: flex-start;
          max-width: calc(100% - 60px);

          .message-bubble {
            background-color: #f3f4f6;
            color: #1f2937;
            border-radius: 18px 18px 18px 4px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

            .dark-mode & {
              background-color: #374151;
              color: #e5e7eb;
              box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
            }
          }
        }
      }

      .message-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        overflow: hidden;
        flex-shrink: 0;
        border: 2px solid #e5e7eb;

        .dark-mode & {
          border-color: #4b5563;
        }

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .avatar-placeholder {
          width: 100%;
          height: 100%;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
          font-size: 16px;

          &.assistant-avatar {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
          }
        }
      }

      .message-content {
        display: flex;
        flex-direction: column;

        .message-bubble {
          padding: 14px 18px;
          line-height: 1.6;
          word-wrap: break-word;

          // Markdown 内容样式
          :deep(p) {
            margin: 0 0 8px 0;

            &:last-child {
              margin-bottom: 0;
            }
          }

          :deep(h1),
          :deep(h2),
          :deep(h3),
          :deep(h4),
          :deep(h5),
          :deep(h6) {
            margin: 12px 0 8px 0;
            font-weight: 600;
            line-height: 1.3;

            &:first-child {
              margin-top: 0;
            }
          }

          :deep(h1) {
            font-size: 1.5em;
            border-bottom: 1px solid rgba(0, 0, 0, 0.1);
            padding-bottom: 4px;

            .dark-mode & {
              border-bottom-color: rgba(255, 255, 255, 0.1);
            }
          }

          :deep(h2) {
            font-size: 1.3em;
          }

          :deep(h3) {
            font-size: 1.1em;
          }

          :deep(ul),
          :deep(ol) {
            margin: 8px 0;
            padding-left: 20px;
          }

          :deep(li) {
            margin: 4px 0;
          }

          :deep(code) {
            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
            font-size: 0.9em;
            background-color: rgba(0, 0, 0, 0.06);
            padding: 2px 6px;
            border-radius: 4px;

            .dark-mode & {
              background-color: rgba(255, 255, 255, 0.1);
            }

            .user-message & {
              background-color: rgba(255, 255, 255, 0.2);
            }
          }

          :deep(pre) {
            margin: 12px 0;
            padding: 16px;
            background-color: #1e1e1e;
            border-radius: 8px;
            overflow-x: auto;

            .user-message & {
              background-color: #0d1117;
            }

            code {
              background-color: transparent;
              padding: 0;
              font-size: 14px;
              line-height: 1.5;

              .hljs {
                background: transparent;
              }
            }
          }

          :deep(blockquote) {
            margin: 12px 0;
            padding: 8px 16px;
            border-left: 4px solid #409eff;
            background-color: rgba(64, 158, 255, 0.05);
            color: #606266;

            .dark-mode & {
              background-color: rgba(64, 158, 255, 0.1);
              color: #c0c4cc;
            }
          }

          :deep(a) {
            color: #409eff;
            text-decoration: none;

            &:hover {
              text-decoration: underline;
            }

            .dark-mode & {
              color: #66b1ff;
            }
          }

          :deep(table) {
            margin: 12px 0;
            border-collapse: collapse;
            width: 100%;
            font-size: 0.9em;

            th,
            td {
              padding: 8px 12px;
              border: 1px solid rgba(0, 0, 0, 0.1);
              text-align: left;
            }

            th {
              background-color: rgba(0, 0, 0, 0.05);
              font-weight: 600;

              .dark-mode & {
                background-color: rgba(255, 255, 255, 0.1);
              }
            }

            tr:nth-child(even) {
              background-color: rgba(0, 0, 0, 0.02);

              .dark-mode & {
                background-color: rgba(255, 255, 255, 0.02);
              }
            }
          }

          :deep(hr) {
            margin: 16px 0;
            border: none;
            border-top: 1px solid rgba(0, 0, 0, 0.1);

            .dark-mode & {
              border-top-color: rgba(255, 255, 255, 0.1);
            }
          }

          :deep(img) {
            max-width: 100%;
            height: auto;
            border-radius: 8px;
            margin: 8px 0;
          }
        }

        .streaming-bubble {
          position: relative;
        }

        .message-time {
          font-size: 12px;
          color: #9ca3af;
          margin-top: 6px;

          .dark-mode & {
            color: #6b7280;
          }
        }
      }
      
      .typing-indicator {
        display: flex;
        align-items: center;
        padding: 14px 18px;
        background-color: #f3f4f6;
        border-radius: 18px 18px 18px 4px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

        .dark-mode & {
          background-color: #374151;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
        }

        .typing-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background-color: #9ca3af;
          margin: 0 3px;
          animation: typing 1.4s infinite ease-in-out both;

          &:nth-child(1) {
            animation-delay: -0.32s;
          }
          &:nth-child(2) {
            animation-delay: -0.16s;
          }
        }
      }
    }

    .load-more {
      text-align: center;
      margin: 20px 0;
    }
  }
  
  .message-input-area {
    padding: 20px;
    border-top: 1px solid #e4e7ed;
    display: flex;
    align-items: flex-end;
    gap: 12px;
    
    .dark-mode & {
      border-top-color: #333;
    }
    
    .message-input {
      flex: 1;
      min-height: 40px;
      max-height: 200px;
      overflow-y: auto;
      
      .el-textarea__inner {
        border-radius: 20px;
        resize: none;
        min-height: 40px;
        max-height: 200px;
        padding: 10px 16px;
      }
    }
    
    .send-button {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      padding: 0;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

// 动画
@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

// 滚动条样式
.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track {
  background: transparent;
}

.message-list::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
  
  .dark-mode & {
    background: #555;
  }
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: #909399;
  
  .dark-mode & {
    background: #777;
  }
}
</style>