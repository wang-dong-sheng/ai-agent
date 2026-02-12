import { defineStore } from 'pinia'
import api from '../services/api'

export const useConversationStore = defineStore('conversation', {
  state: () => ({
    // 对话列表
    conversations: [],
    // 当前对话ID
    currentConversationId: '',
    // 当前对话消息
    currentMessages: []
  }),
  
  actions: {
    // 获取对话列表
    async fetchConversations() {
      try {
        // 从本地存储获取用户信息
        const userInfo = JSON.parse(localStorage.getItem('user'))
        if (!userInfo || !userInfo.id) {
          throw new Error('用户未登录')
        }
        
        const response = await api.get('/conversation/list', {
          params: { userId: userInfo.id }
        })
        
        console.log('获取对话列表响应:', response.data)
        
        // 确保 conversations 始终是数组
        if (Array.isArray(response.data)) {
          // 如果是字符串数组，转换为对象数组
          this.conversations = response.data.map(id => ({
            id: id,
            title: '对话 ' + id.substring(id.length - 8),
            updatedAt: new Date().toISOString(),
            createdAt: new Date().toISOString()
          }))
        } else if (response.data && response.data.data) {
          // 处理后端返回的标准格式 { success: true, data: [] }
          const data = response.data.data
          if (Array.isArray(data)) {
            // 如果是字符串数组，转换为对象数组
            this.conversations = data.map(id => ({
              id: id,
              title: '对话 ' + id.substring(id.length - 8),
              updatedAt: new Date().toISOString(),
              createdAt: new Date().toISOString()
            }))
          } else {
            this.conversations = []
          }
        } else {
          this.conversations = []
        }
        
        console.log('处理后的对话列表:', this.conversations)
        return this.conversations
      } catch (error) {
        console.error('获取对话列表失败:', error)
        // 确保出错时 conversations 仍然是数组
        this.conversations = []
        throw error
      }
    },
    
    // 创建新对话
    async createConversation() {
      try {
        // 模拟创建对话（实际项目中应调用后端API）
        const newConversation = {
          id: Date.now().toString(),
          userId: JSON.parse(localStorage.getItem('user'))?.id || '1',
          title: '新对话',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
        
        // 添加到对话列表顶部
        this.conversations.unshift(newConversation)
        
        // 设置为当前对话
        this.currentConversationId = newConversation.id
        this.currentMessages = []
        
        return newConversation
      } catch (error) {
        console.error('创建对话失败:', error)
        throw error
      }
    },
    
    // 删除对话
    async deleteConversation(conversationId) {
      try {
        // 从对话列表中移除
        this.conversations = this.conversations.filter(c => c.id !== conversationId)
        
        // 如果删除的是当前对话，清空当前对话状态
        if (this.currentConversationId === conversationId) {
          this.currentConversationId = ''
          this.currentMessages = []
        }
        
        return true
      } catch (error) {
        console.error('删除对话失败:', error)
        throw error
      }
    },
    
    // 获取对话消息
    async fetchMessages(conversationId) {
      try {
        // 从本地存储获取用户信息
        const userInfo = JSON.parse(localStorage.getItem('user'))
        if (!userInfo || !userInfo.id) {
          throw new Error('用户未登录')
        }
        
        const response = await api.get('/conversation/messages', {
          params: {
            userId: userInfo.id,
            conversationId
          }
        })
        // 确保 currentMessages 始终是数组
        if (Array.isArray(response.data)) {
          this.currentMessages = response.data
        } else if (response.data && response.data.data) {
          // 处理后端返回的标准格式 { success: true, data: [] }
          this.currentMessages = Array.isArray(response.data.data) ? response.data.data : []
        } else {
          this.currentMessages = []
        }
        this.currentConversationId = conversationId
        return this.currentMessages
      } catch (error) {
        console.error('获取消息失败:', error)
        // 确保出错时 currentMessages 仍然是数组
        this.currentMessages = []
        throw error
      }
    },
    
    // 发送消息
    async sendMessage({ conversationId, content }) {
      try {
        // 从本地存储获取用户信息
        const userInfo = JSON.parse(localStorage.getItem('user'))
        if (!userInfo || !userInfo.id) {
          throw new Error('用户未登录')
        }
        
        // 创建用户消息
        const userMessage = {
          id: Date.now().toString(),
          conversationId,
          role: 'user',
          content: content,
          createdAt: new Date().toISOString()
        }
        
        // 添加用户消息
        this.currentMessages.push(userMessage)
        
        // 导入 chatAPI
        const { chatAPI } = await import('../services/api')
        
        // 保存当前上下文
        const self = this;
        
        // 使用fetch API获取SSE流
        return new Promise((resolve, reject) => {
          let aiResponseContent = ''
          let aiResponseId = (Date.now() + 1).toString()
          let aiResponseCreatedAt = new Date().toISOString()
          
          // 创建临时AI消息（用于显示流式响应）
          const tempAiResponse = {
            id: aiResponseId,
            conversationId,
            role: 'assistant',
            content: '',
            createdAt: aiResponseCreatedAt,
            isStreaming: true
          }
          self.currentMessages.push(tempAiResponse)
          
          // 构建请求URL
          const url = new URL('http://localhost:8123/api/ai/love_app/chat/agent/sse');
          url.searchParams.append('message', content);
          url.searchParams.append('chatId', conversationId);
          url.searchParams.append('userId', userInfo.id);
          
          console.log('发送请求到:', url.toString());
          
          // 发送请求
          fetch(url.toString(), {
            method: 'GET',
            headers: {
              'Accept': 'text/event-stream',
              'Cache-Control': 'no-cache',
              'Connection': 'keep-alive'
            }
          })
          .then(response => {
            console.log('收到响应:', response.status, response.statusText);
            
            if (!response.ok) {
              throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            if (!response.body) {
              throw new Error('No response body');
            }
            
            // 获取可读流
            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let buffer = '';
            
            // 处理流
            function processStream() {
              reader.read().then(({ done, value }) => {
                if (done) {
                  console.log('流结束');
                  // 流结束，更新AI消息
                  const aiResponse = {
                    id: aiResponseId,
                    conversationId,
                    role: 'assistant',
                    content: aiResponseContent,
                    createdAt: aiResponseCreatedAt
                  };
                  
                  // 替换临时消息
                  const index = self.currentMessages.findIndex(msg => msg.id === aiResponseId);
                  if (index !== -1) {
                    self.currentMessages.splice(index, 1, aiResponse);
                  }
                  
                  resolve(userMessage);
                  return;
                }
                
                // 处理接收到的数据
                const chunk = decoder.decode(value, { stream: true });
                buffer += chunk;
                console.log('接收到数据:', chunk);
                
                // 解析SSE格式
                const lines = buffer.split('\n');
                for (const line of lines) {
                  if (line.startsWith('data:')) {
                    const dataContent = line.substring(5).trim();
                    aiResponseContent += dataContent;
                    console.log('解析到数据:', dataContent);
                  }
                }
                
                // 更新临时消息内容
                const index = self.currentMessages.findIndex(msg => msg.id === aiResponseId);
                if (index !== -1) {
                  self.currentMessages[index].content = aiResponseContent;
                  console.log('更新消息内容:', aiResponseContent);
                }
                
                // 继续处理流
                processStream();
              })
              .catch(error => {
                console.error('处理流失败:', error);
                reject(error);
              });
            }
            
            // 开始处理流
            processStream();
          })
          .catch(error => {
            console.error('获取SSE流失败:', error);
            reject(error);
          });
        })
      } catch (error) {
        console.error('发送消息失败:', error)
        throw error
      }
    },
    
    // 重命名对话
    async renameConversation({ id, title }) {
      try {
        // 更新对话列表中的标题
        const conversation = this.conversations.find(c => c.id === id)
        if (conversation) {
          conversation.title = title
        }
        
        return { id, title }
      } catch (error) {
        console.error('重命名对话失败:', error)
        throw error
      }
    }
  }
})