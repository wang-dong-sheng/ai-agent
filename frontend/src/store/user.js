import { defineStore } from 'pinia'
import api from '../services/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 用户信息
    user: {
      id: '',
      username: '',
      nickname: '',
      avatar: ''
    },
    // Token
    token: '',
    // 登录状态
    isLoggedIn: false
  }),
  
  actions: {
    // 登录
    async login(credentials) {
      try {
        const response = await api.post('/user/login', credentials)
        
        // 处理后端返回的响应格式
        if (response.data && response.data.success) {
          const userData = response.data.data
          
          // 模拟token（实际项目中应从后端获取）
          const token = 'mock-token-' + Date.now()
          
          // 保存token
          this.token = token
          localStorage.setItem('token', token)
          
          // 保存用户信息
          this.user = userData
          localStorage.setItem('user', JSON.stringify(userData))
          
          // 设置登录状态
          this.isLoggedIn = true
          
          return response.data
        } else {
          throw new Error(response.data.message || '登录失败')
        }
      } catch (error) {
        console.error('登录失败:', error)
        throw error
      }
    },
    
    // 注册
    async register(userData) {
      try {
        const response = await api.post('/user/register', userData)
        
        // 处理后端返回的响应格式
        if (response.data && response.data.success) {
          return response.data
        } else {
          throw new Error(response.data.message || '注册失败')
        }
      } catch (error) {
        console.error('注册失败:', error)
        throw error
      }
    },
    
    // 退出登录
    logout() {
      // 清除状态
      this.token = ''
      this.user = {
        id: '',
        username: '',
        nickname: '',
        avatar: ''
      }
      this.isLoggedIn = false
      
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
    
    // 初始化用户状态
    initUser() {
      try {
        // 从本地存储加载token
        const savedToken = localStorage.getItem('token')
        if (savedToken) {
          this.token = savedToken
          this.isLoggedIn = true
          
          // 从本地存储加载用户信息
          const savedUser = localStorage.getItem('user')
          if (savedUser) {
            this.user = JSON.parse(savedUser)
          }
        }
      } catch (error) {
        console.error('初始化用户状态失败:', error)
        // 清除可能损坏的本地存储
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        this.token = ''
        this.user = {
          id: '',
          username: '',
          nickname: '',
          avatar: ''
        }
        this.isLoggedIn = false
      }
    }
  }
})