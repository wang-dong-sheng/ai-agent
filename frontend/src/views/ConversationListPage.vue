<template>
  <div class="conversation-list-container">
    <div class="header">
      <h1>历史对话</h1>
      <div class="user-info">
        <span class="welcome">欢迎，{{ userInfo?.username }}</span>
        <button class="logout-btn" @click="handleLogout">登出</button>
      </div>
    </div>
    
    <div class="create-conversation">
      <button class="create-btn" @click="createNewConversation">
        + 新建对话
      </button>
    </div>
    
    <div v-if="loading" class="loading">
      加载中...
    </div>
    
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-else-if="conversations.length === 0" class="empty-state">
      暂无历史对话，点击上方按钮新建对话
    </div>
    
    <div v-else class="conversation-list">
      <div 
        v-for="(conversationId, index) in conversations" 
        :key="conversationId"
        class="conversation-item"
        @click="navigateToConversation(conversationId)"
      >
        <div class="conversation-info">
          <div class="conversation-id">{{ conversationId }}</div>
          <div class="conversation-index">对话 {{ conversations.length - index }}</div>
        </div>
        <div class="conversation-actions">
          <button class="view-btn">查看</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { conversationAPI } from '../services/api'

export default {
  name: 'ConversationListPage',
  setup() {
    const router = useRouter()
    const userInfo = ref(null)
    const conversations = ref([])
    const loading = ref(false)
    const error = ref('')

    // 获取用户信息
    const getUserInfo = () => {
      const storedUserInfo = localStorage.getItem('userInfo')
      if (storedUserInfo) {
        userInfo.value = JSON.parse(storedUserInfo)
      } else {
        router.push('/login')
      }
    }

    // 获取会话列表
    const fetchConversations = async () => {
      if (!userInfo.value) return
      
      loading.value = true
      error.value = ''
      
      try {
        const response = await conversationAPI.getConversationList(userInfo.value.id)
        if (response.data.success) {
          conversations.value = response.data.data
        } else {
          error.value = response.data.message
        }
      } catch (err) {
        error.value = '获取会话列表失败'
        console.error('Fetch conversations error:', err)
      } finally {
        loading.value = false
      }
    }

    // 跳转到会话详情页
    const navigateToConversation = (conversationId) => {
      router.push(`/conversation/${conversationId}`)
    }

    // 创建新会话
    const createNewConversation = () => {
      // 生成唯一的会话ID
      const newConversationId = 'conv_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
      router.push(`/conversation/${newConversationId}`)
    }

    // 登出
    const handleLogout = () => {
      localStorage.removeItem('userInfo')
      router.push('/login')
    }

    // 组件挂载时获取用户信息和会话列表
    onMounted(() => {
      getUserInfo()
      fetchConversations()
    })

    return {
      userInfo,
      conversations,
      loading,
      error,
      navigateToConversation,
      createNewConversation,
      handleLogout
    }
  }
}
</script>

<style scoped>
.conversation-list-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.header h1 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome {
  color: #666;
}

.logout-btn {
  padding: 8px 16px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: #d32f2f;
}

.create-conversation {
  margin-bottom: 30px;
}

.create-btn {
  padding: 12px 24px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: background-color 0.3s;
}

.create-btn:hover {
  background-color: #45a049;
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

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px dashed #ddd;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.conversation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #eee;
}

.conversation-item:hover {
  background-color: #f0f0f0;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.conversation-info {
  flex: 1;
}

.conversation-id {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-index {
  font-size: 14px;
  color: #666;
}

.conversation-actions {
  display: flex;
  gap: 10px;
}

.view-btn {
  padding: 6px 12px;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.view-btn:hover {
  background-color: #1976D2;
}

@media (max-width: 600px) {
  .conversation-list-container {
    padding: 10px;
  }
  
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .user-info {
    align-self: stretch;
    justify-content: space-between;
  }
}
</style>
