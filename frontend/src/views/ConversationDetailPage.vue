<template>
  <div class="conversation-detail-container">
    <div class="header">
      <div class="back-btn" @click="goBack">
        ← 返回历史对话
      </div>
      <h2>对话详情</h2>
      <div class="conversation-id-display">
        会话ID: {{ conversationId }}
      </div>
    </div>
    
    <div class="chat-container">
      <!-- 消息列表 -->
      <div class="message-list">
        <div v-if="loadingMessages" class="loading">
          加载消息中...
        </div>
        
        <div v-else-if="errorMessages" class="error-message">
          {{ errorMessages }}
        </div>
        
        <div v-else-if="messages.length === 0" class="empty-messages">
          暂无消息，开始发送第一条消息吧！
        </div>
        
        <div v-else class="messages">
          <div 
            v-for="(message, index) in messages" 
            :key="index"
            :class="['message-item', message.messageType.toLowerCase()]"
          >
            <div class="message-header">
              <span class="message-type">
                {{ message.messageType === 'USER' ? '用户' : message.messageType === 'ASSISTANT' ? '助手' : '系统' }}
              </span>
              <span class="message-time">
                {{ formatTime(message.createdAt) }}
              </span>
            </div>
            <div class="message-content">
              {{ message.content }}
            </div>
          </div>
          
          <!-- 助手正在输入 -->
          <div v-if="assistantTyping" class="message-item assistant typing">
            <div class="message-header">
              <span class="message-type">助手</span>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="input-area">
        <form @submit.prevent="handleSendMessage">
          <input 
            type="text" 
            v-model="inputMessage" 
            placeholder="输入消息..." 
            class="message-input"
            :disabled="sending"
          >
          <button 
            type="submit" 
            class="send-btn" 
            :disabled="sending || !inputMessage.trim()"
          >
            {{ sending ? '发送中...' : '发送' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { conversationAPI, chatAPI } from '../services/api'

export default {
  name: 'ConversationDetailPage',
  props: {
    id: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const router = useRouter()
    const route = useRoute()
    const conversationId = computed(() => props.id || route.params.id)
    const userInfo = ref(null)
    const messages = ref([])
    const inputMessage = ref('')
    const loadingMessages = ref(false)
    const errorMessages = ref('')
    const sending = ref(false)
    const assistantTyping = ref(false)

    // 获取用户信息
    const getUserInfo = () => {
      const storedUserInfo = localStorage.getItem('userInfo')
      if (storedUserInfo) {
        userInfo.value = JSON.parse(storedUserInfo)
      } else {
        router.push('/login')
      }
    }

    // 加载会话消息
    const loadMessages = async () => {
      if (!userInfo.value) return
      
      loadingMessages.value = true
      errorMessages.value = ''
      
      try {
        const response = await conversationAPI.getConversationMessages(
          userInfo.value.id,
          conversationId.value
        )
        if (response.data.success) {
          messages.value = response.data.data
        } else {
          errorMessages.value = response.data.message
        }
      } catch (err) {
        errorMessages.value = '加载消息失败'
        console.error('Load messages error:', err)
      } finally {
        loadingMessages.value = false
      }
    }

    // 发送消息
    const handleSendMessage = async () => {
      if (!inputMessage.value.trim() || !userInfo.value) return
      
      const messageText = inputMessage.value.trim()
      inputMessage.value = ''
      sending.value = true
      assistantTyping.value = true
      
      // 添加用户消息到本地
      const userMessage = {
        messageType: 'USER',
        content: messageText,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
      messages.value.push(userMessage)
      
      try {
        // 调用后端API发送消息
        const response = await chatAPI.doChatSync(
          messageText,
          conversationId.value,
          userInfo.value.id
        )
        
        if (response.data) {
          // 添加助手消息到本地
          const assistantMessage = {
            messageType: 'ASSISTANT',
            content: response.data,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          }
          messages.value.push(assistantMessage)
        }
      } catch (err) {
        console.error('Send message error:', err)
        // 可以在这里添加错误处理，例如显示错误消息
      } finally {
        sending.value = false
        assistantTyping.value = false
      }
    }

    // 格式化时间
    const formatTime = (timeString) => {
      if (!timeString) return ''
      const date = new Date(timeString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    // 返回上一页
    const goBack = () => {
      router.push('/conversations')
    }

    // 组件挂载时获取用户信息和加载消息
    onMounted(() => {
      getUserInfo()
      loadMessages()
    })

    return {
      conversationId,
      userInfo,
      messages,
      inputMessage,
      loadingMessages,
      errorMessages,
      sending,
      assistantTyping,
      handleSendMessage,
      formatTime,
      goBack
    }
  }
}
</script>

<style scoped>
.conversation-detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
  gap: 20px;
}

.back-btn {
  cursor: pointer;
  color: #2196F3;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.back-btn:hover {
  text-decoration: underline;
}

.header h2 {
  font-size: 20px;
  color: #333;
  margin: 0;
  flex: 1;
}

.conversation-id-display {
  font-size: 14px;
  color: #666;
  background-color: #f5f5f5;
  padding: 6px 12px;
  border-radius: 4px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f9f9f9;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.empty-messages {
  text-align: center;
  padding: 60px 20px;
  color: #666;
  font-style: italic;
}

.messages {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-item {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 8px;
  position: relative;
}

.message-item.user {
  align-self: flex-end;
  background-color: #e3f2fd;
  border-bottom-right-radius: 2px;
}

.message-item.assistant {
  align-self: flex-start;
  background-color: white;
  border-bottom-left-radius: 2px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.message-item.system {
  align-self: center;
  background-color: #f5f5f5;
  font-size: 14px;
  color: #666;
  max-width: 90%;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
  color: #666;
}

.message-type {
  font-weight: 500;
}

.message-time {
  font-size: 11px;
  opacity: 0.8;
}

.message-content {
  font-size: 16px;
  line-height: 1.4;
  color: #333;
  word-break: break-word;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-indicator .dot {
  width: 8px;
  height: 8px;
  background-color: #666;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.input-area {
  padding: 20px;
  background-color: white;
  border-top: 1px solid #eee;
}

.input-area form {
  display: flex;
  gap: 10px;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 24px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s;
}

.message-input:focus {
  border-color: #2196F3;
}

.message-input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.send-btn {
  padding: 0 24px;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.send-btn:hover:not(:disabled) {
  background-color: #1976D2;
}

.send-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

@media (max-width: 600px) {
  .conversation-detail-container {
    padding: 10px;
  }
  
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .conversation-id-display {
    align-self: stretch;
    text-align: center;
  }
  
  .message-item {
    max-width: 90%;
  }
}
</style>
