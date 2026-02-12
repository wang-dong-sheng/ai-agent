import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8123/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 从本地存储获取token
    const token = localStorage.getItem('token')
    if (token) {
      // 添加到请求头
      config.headers.Authorization = `Bearer ${token}`
    }
    // 确保添加 Content-Type 头
    if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json'
    }
    // 确保添加 Accept 头
    if (!config.headers['Accept']) {
      config.headers['Accept'] = '*/*'
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    // 处理401错误
    if (error.response && error.response.status === 401) {
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // 跳转到登录页
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 用户相关API
export const userAPI = {
  // 用户注册
  register: (username, password) => {
    return api.post('/user/register', {
      username,
      password
    })
  },
  
  // 用户登录
  login: (username, password) => {
    return api.post('/user/login', {
      username,
      password
    })
  },
  
  // 获取用户信息
  getUserInfo: (id) => {
    return api.get('/user/info', {
      params: { id }
    })
  }
}

// 会话相关API
export const conversationAPI = {
  // 获取用户会话列表
  getConversationList: (userId) => {
    return api.get('/conversation/list', {
      params: { userId }
    })
  },
  
  // 获取会话消息详情
  getConversationMessages: (userId, conversationId) => {
    return api.get('/conversation/messages', {
      params: {
        userId,
        conversationId
      }
    })
  }
}

// 聊天相关API
export const chatAPI = {
  // 同步聊天
  doChatSync: (message, chatId, userId) => {
    return api.get('/ai/love_app/chat/sync', {
      params: {
        message,
        chatId,
        userId
      }
    })
  },
  
  // 流式聊天（带工具）
  doChatWithToolsStream: (message, chatId, userId) => {
    return api.get('/ai/love_app/chat/tool/sse', {
      params: {
        message,
        chatId,
        userId
      },
      responseType: 'stream'
    })
  },
  
  // 流式聊天（带RAG）
  doChatWithRagStream: (message, chatId, userId) => {
    return api.get('/ai/love_app/chat/rag/sse', {
      params: {
        message,
        chatId,
        userId
      },
      responseType: 'stream'
    })
  },
  
  // 流式聊天（带Agent）
  doChatWithAgentStream: (message, chatId, userId) => {
    return api.get('/ai/love_app/chat/agent/sse', {
      params: {
        message,
        chatId,
        userId
      },
      responseType: 'stream'
    })
  }
}

export default api
