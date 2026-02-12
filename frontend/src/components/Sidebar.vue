<template>
  <div class="sidebar" :class="{ 'collapsed': collapsed, 'dark-mode': isDarkMode }">
    <!-- 顶部区域 -->
    <div class="sidebar-header">
      <h2 v-if="!collapsed" class="sidebar-title">AI助手</h2>
    </div>
    
    <!-- 新建对话按钮 -->
    <div class="new-chat-button">
      <el-button type="primary" round @click="createNewConversation">
        <el-icon><Plus /></el-icon>
        <span v-if="!collapsed">新建对话</span>
      </el-button>
    </div>
    
    <!-- 历史对话列表 -->
    <div class="conversation-list">
      <div 
        v-for="conversation in conversations" 
        :key="conversation.id"
        class="conversation-item"
        :class="{ 'active': currentConversationId === conversation.id }"
        @click="selectConversation(conversation.id)"
      >
        <div class="conversation-content">
          <h3 class="conversation-title">{{ getConversationTitle(conversation) }}</h3>
          <p v-if="!collapsed" class="conversation-time">{{ formatTime(conversation.updatedAt) }}</p>
        </div>
        <el-button 
          v-if="!collapsed" 
          type="text" 
          size="small" 
          class="delete-button"
          @click.stop="deleteConversation(conversation.id)"
        >
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
      
      <!-- 空状态 -->
      <div v-if="conversations.length === 0" class="empty-state">
        <el-icon class="empty-icon"><ChatLineSquare /></el-icon>
        <p v-if="!collapsed">暂无对话</p>
        <p v-if="!collapsed" class="empty-hint">点击新建对话开始</p>
      </div>
    </div>
    
    <!-- 用户信息区 -->
    <div class="user-info">
      <div class="user-avatar" @click="toggleUserMenu">
        <img v-if="user.avatar" :src="user.avatar" :alt="user.nickname || user.username" />
        <div v-else class="avatar-placeholder">
          {{ getUserInitial(user) }}
        </div>
      </div>
      <div v-if="!collapsed" class="user-details">
        <p class="user-name">{{ user.nickname || user.username }}</p>
      </div>
      <el-dropdown v-if="!collapsed" trigger="click" @command="handleUserMenuCommand">
        <el-button type="text" size="small">
          <el-icon><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人设置</el-dropdown-item>
            <el-dropdown-item command="theme">
              <span>{{ isDarkMode ? '切换到浅色模式' : '切换到深色模式' }}</span>
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <span style="color: #f56c6c">退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useConversationStore } from '../store/conversation'
import { useUserStore } from '../store/user'
import { useAppStore } from '../store/app'
import { Plus, Delete, ChatLineSquare, ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle'])

const router = useRouter()
const conversationStore = useConversationStore()
const userStore = useUserStore()
const appStore = useAppStore()

// 状态
const userMenuVisible = ref(false)

// 计算属性
const conversations = computed(() => conversationStore.conversations)
const currentConversationId = computed(() => conversationStore.currentConversationId)
const user = computed(() => userStore.user)
const isDarkMode = computed(() => appStore.darkMode)

// 方法
const createNewConversation = async () => {
  try {
    const newConversation = await conversationStore.createConversation()
    selectConversation(newConversation.id)
  } catch (error) {
    console.error('创建对话失败:', error)
  }
}

const selectConversation = (conversationId) => {
  conversationStore.currentConversationId = conversationId
  router.push(`/chat/${conversationId}`)
}

const deleteConversation = (conversationId) => {
  ElMessageBox.confirm(
    '确定要删除这个对话吗？',
    '删除对话',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await conversationStore.deleteConversation(conversationId)
      // 如果删除的是当前对话，切换到最新的对话
      if (currentConversationId.value === conversationId && conversations.value.length > 0) {
        selectConversation(conversations.value[0].id)
      } else if (conversations.value.length === 0) {
        router.push('/chat')
      }
    } catch (error) {
      console.error('删除对话失败:', error)
    }
  }).catch(() => {
    // 取消删除
  })
}

const getConversationTitle = (conversation) => {
  if (conversation.title) return conversation.title
  if (conversation.messages && conversation.messages.length > 0) {
    const firstMessage = conversation.messages.find(msg => msg.role === 'user')
    if (firstMessage) {
      return firstMessage.content.substring(0, 30) + (firstMessage.content.length > 30 ? '...' : '')
    }
  }
  return '新对话'
}

const formatTime = (timestamp) => {
  const now = new Date()
  const date = new Date(timestamp)
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)
  
  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`
  return date.toLocaleDateString()
}

const getUserInitial = (user) => {
  if (user.nickname) return user.nickname.charAt(0).toUpperCase()
  if (user.username) return user.username.charAt(0).toUpperCase()
  return 'U'
}

const toggleUserMenu = () => {
  userMenuVisible.value = !userMenuVisible.value
}

const handleUserMenuCommand = (command) => {
  switch (command) {
    case 'profile':
      // 跳转到个人设置页面
      break
    case 'theme':
      appStore.toggleDarkMode()
      break
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
  }
}

// 生命周期
onMounted(() => {
  // 加载对话列表
  conversationStore.fetchConversations()
})
</script>

<style lang="scss" scoped>
.sidebar {
  width: 280px;
  height: 100vh;
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  position: relative;
  z-index: 999;
  
  &.collapsed {
    width: 64px;
  }
  
  &.dark-mode {
    background-color: #1f1f1f;
    border-right-color: #333;
  }
  
  .sidebar-header {
    padding: 20px;
    border-bottom: 1px solid #e4e7ed;
    
    .dark-mode & {
      border-bottom-color: #333;
    }
    
    .sidebar-title {
      margin: 0;
      font-size: 18px;
      font-weight: bold;
      color: #409eff;
    }
  }
  
  .new-chat-button {
    padding: 16px;
    
    .el-button {
      width: 100%;
    }
  }
  
  .conversation-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px 0;
    
    .conversation-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      cursor: pointer;
      transition: all 0.2s ease;
      
      &:hover {
        background-color: #ecf5ff;
        
        .dark-mode & {
          background-color: #2c3e50;
        }
      }
      
      &.active {
        background-color: #e6f7ff;
        
        .dark-mode & {
          background-color: #34495e;
        }
      }
      
      .conversation-content {
        flex: 1;
        min-width: 0;
        
        .conversation-title {
          margin: 0 0 4px 0;
          font-size: 14px;
          font-weight: 500;
          color: #303133;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          
          .dark-mode & {
            color: #e0e0e0;
          }
        }
        
        .conversation-time {
          margin: 0;
          font-size: 12px;
          color: #909399;
          
          .dark-mode & {
            color: #909090;
          }
        }
      }
      
      .delete-button {
        color: #909399;
        
        &:hover {
          color: #f56c6c;
        }
      }
    }
    
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      
      .empty-icon {
        font-size: 48px;
        color: #c0c4cc;
        margin-bottom: 16px;
        
        .dark-mode & {
          color: #555;
        }
      }
      
      p {
        margin: 4px 0;
        color: #909399;
        
        .dark-mode & {
          color: #909090;
        }
      }
      
      .empty-hint {
        font-size: 12px;
      }
    }
  }
  
  .user-info {
    padding: 16px;
    border-top: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    
    .dark-mode & {
      border-top-color: #333;
    }
    
    .user-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      overflow: hidden;
      cursor: pointer;
      margin-right: 12px;
      
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
        font-size: 16px;
      }
    }
    
    .user-details {
      flex: 1;
      min-width: 0;
      
      .user-name {
        margin: 0;
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        
        .dark-mode & {
          color: #e0e0e0;
        }
      }
    }
  }
}

// 滚动条样式
.conversation-list::-webkit-scrollbar {
  width: 4px;
}

.conversation-list::-webkit-scrollbar-track {
  background: transparent;
}

.conversation-list::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 2px;
  
  .dark-mode & {
    background: #555;
  }
}

.conversation-list::-webkit-scrollbar-thumb:hover {
  background: #909399;
  
  .dark-mode & {
    background: #777;
  }
}
</style>