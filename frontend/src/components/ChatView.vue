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

const formatMessageContent = (content) => {
  // 处理Markdown和代码高亮
  if (!content) return ''
  
  // 简单的Markdown处理
  let formattedContent = content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2" target="_blank">$1</a>')
    .replace(/\n/g, '<br>')
  
  // 处理代码块
  formattedContent = formattedContent.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
    const highlightedCode = hljs.highlight(code, { language: lang || 'plaintext' }).value
    return `<pre><code class="language-${lang || 'plaintext'}">${highlightedCode}</code></pre>`
  })
  
  return formattedContent
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
      margin-bottom: 20px;
      
      &.user-message {
        flex-direction: row-reverse;
        
        .message-content {
          align-items: flex-end;
          
          .message-bubble {
            background-color: #409eff;
            color: #fff;
            border-radius: 18px 4px 18px 18px;
          }
        }
      }
      
      &.assistant-message {
        flex-direction: row;
        
        .message-content {
          align-items: flex-start;
          
          .message-bubble {
            background-color: #f5f7fa;
            color: #303133;
            border-radius: 4px 18px 18px 18px;
            
            .dark-mode & {
              background-color: #2c2c2c;
              color: #e0e0e0;
            }
          }
        }
      }
      
      .message-avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        overflow: hidden;
        margin: 0 12px;
        flex-shrink: 0;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        .avatar-placeholder {
          width: 100%;
          height: 100%;
          background-color: #409eff;
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
          font-size: 14px;
          
          &.assistant-avatar {
            background-color: #909399;
          }
        }
      }
      
      .message-content {
        display: flex;
        flex-direction: column;
        max-width: 70%;
        
        .message-bubble {
          padding: 12px 16px;
          line-height: 1.5;
          word-wrap: break-word;
          
          pre {
            margin: 8px 0;
            padding: 12px;
            background-color: rgba(0, 0, 0, 0.05);
            border-radius: 4px;
            overflow-x: auto;
            
            .dark-mode & {
              background-color: rgba(0, 0, 0, 0.2);
            }
            
            code {
              font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
              font-size: 14px;
            }
          }
          
          a {
            color: #409eff;
            text-decoration: none;
            
            &:hover {
              text-decoration: underline;
            }
            
            .dark-mode & {
              color: #66b1ff;
            }
          }
        }
        
        .streaming-bubble {
          position: relative;
        }
        
        .message-time {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
          
          .dark-mode & {
            color: #909090;
          }
        }
      }
      
      .typing-indicator {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        background-color: #f5f7fa;
        border-radius: 4px 18px 18px 18px;
        
        .dark-mode & {
          background-color: #2c2c2c;
        }
        
        .typing-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background-color: #909399;
          margin: 0 2px;
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