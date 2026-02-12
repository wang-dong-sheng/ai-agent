import axios from 'axios';

// 基础 URL
const BASE_URL = 'http://localhost:8123/api';

// 创建 axios 实例
const api = axios.create({
  baseURL: BASE_URL,
  timeout: 30000
});

// 测试数据
const testData = {
  username: `testuser_${Date.now()}`,
  password: 'testpassword123',
  message: '你好，我是测试用户，请问你是谁？',
  chatId: `test_chat_${Date.now()}`
};

// 存储用户信息
let userInfo = null;

// 测试注册接口
async function testRegister() {
  console.log('=== 测试注册接口 ===');
  try {
    const response = await api.post('/user/register', {
      username: testData.username,
      password: testData.password
    });
    
    console.log('注册响应:', response.data);
    
    if (response.data.success) {
      userInfo = response.data.data;
      console.log('注册成功，用户信息:', userInfo);
      return true;
    } else {
      console.error('注册失败:', response.data.message);
      return false;
    }
  } catch (error) {
    console.error('注册请求失败:', error.message);
    return false;
  }
}

// 测试登录接口
async function testLogin() {
  console.log('\n=== 测试登录接口 ===');
  try {
    const response = await api.post('/user/login', {
      username: testData.username,
      password: testData.password
    });
    
    console.log('登录响应:', response.data);
    
    if (response.data.success) {
      userInfo = response.data.data;
      console.log('登录成功，用户信息:', userInfo);
      return true;
    } else {
      console.error('登录失败:', response.data.message);
      return false;
    }
  } catch (error) {
    console.error('登录请求失败:', error.message);
    return false;
  }
}

// 测试同步聊天接口
async function testSyncChat() {
  console.log('\n=== 测试同步聊天接口 ===');
  if (!userInfo) {
    console.error('用户未登录，无法测试聊天接口');
    return false;
  }
  
  try {
    const response = await api.get('/ai/love_app/chat/sync', {
      params: {
        message: testData.message,
        chatId: testData.chatId,
        userId: userInfo.id
      }
    });
    
    console.log('同步聊天响应:', response.data);
    console.log('同步聊天成功');
    return true;
  } catch (error) {
    console.error('同步聊天请求失败:', error.message);
    return false;
  }
}

// 测试工具流式聊天接口
async function testToolStreamChat() {
  console.log('\n=== 测试工具流式聊天接口 ===');
  if (!userInfo) {
    console.error('用户未登录，无法测试聊天接口');
    return false;
  }
  
  try {
    const response = await api.get('/ai/love_app/chat/tool/sse', {
      params: {
        message: testData.message,
        chatId: testData.chatId,
        userId: userInfo.id
      },
      responseType: 'stream'
    });
    
    console.log('工具流式聊天响应开始');
    
    // 处理响应流
    const reader = response.data.getReader();
    const decoder = new TextDecoder();
    let responseContent = '';
    
    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        break;
      }
      const chunk = decoder.decode(value, { stream: true });
      responseContent += chunk;
      process.stdout.write(chunk);
    }
    
    console.log('\n工具流式聊天响应结束');
    console.log('工具流式聊天成功');
    return true;
  } catch (error) {
    console.error('工具流式聊天请求失败:', error.message);
    return false;
  }
}

// 测试 RAG 流式聊天接口
async function testRagStreamChat() {
  console.log('\n=== 测试 RAG 流式聊天接口 ===');
  if (!userInfo) {
    console.error('用户未登录，无法测试聊天接口');
    return false;
  }
  
  try {
    const response = await api.get('/ai/love_app/chat/rag/sse', {
      params: {
        message: testData.message,
        chatId: testData.chatId,
        userId: userInfo.id
      },
      responseType: 'stream'
    });
    
    console.log('RAG 流式聊天响应开始');
    
    // 处理响应流
    const reader = response.data.getReader();
    const decoder = new TextDecoder();
    let responseContent = '';
    
    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        break;
      }
      const chunk = decoder.decode(value, { stream: true });
      responseContent += chunk;
      process.stdout.write(chunk);
    }
    
    console.log('\nRAG 流式聊天响应结束');
    console.log('RAG 流式聊天成功');
    return true;
  } catch (error) {
    console.error('RAG 流式聊天请求失败:', error.message);
    return false;
  }
}

// 测试 Agent 流式聊天接口
async function testAgentStreamChat() {
  console.log('\n=== 测试 Agent 流式聊天接口 ===');
  if (!userInfo) {
    console.error('用户未登录，无法测试聊天接口');
    return false;
  }
  
  try {
    const response = await api.get('/ai/love_app/chat/agent/sse', {
      params: {
        message: testData.message,
        chatId: testData.chatId,
        userId: userInfo.id
      },
      responseType: 'stream'
    });
    
    console.log('Agent 流式聊天响应开始');
    
    // 处理响应流
    const reader = response.data.getReader();
    const decoder = new TextDecoder();
    let responseContent = '';
    
    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        break;
      }
      const chunk = decoder.decode(value, { stream: true });
      responseContent += chunk;
      process.stdout.write(chunk);
    }
    
    console.log('\nAgent 流式聊天响应结束');
    console.log('Agent 流式聊天成功');
    return true;
  } catch (error) {
    console.error('Agent 流式聊天请求失败:', error.message);
    return false;
  }
}

// 主测试函数
async function runTests() {
  console.log('开始测试 API 接口...');
  
  // 测试注册
  const registerSuccess = await testRegister();
  if (!registerSuccess) {
    console.error('注册失败，测试终止');
    return;
  }
  
  // 测试登录
  const loginSuccess = await testLogin();
  if (!loginSuccess) {
    console.error('登录失败，测试终止');
    return;
  }
  
  // 测试同步聊天
  await testSyncChat();
  
  // 测试工具流式聊天
  await testToolStreamChat();
  
  // 测试 RAG 流式聊天
  await testRagStreamChat();
  
  // 测试 Agent 流式聊天
  await testAgentStreamChat();
  
  console.log('\n=== 测试完成 ===');
  console.log('测试用户:', testData.username);
  console.log('测试密码:', testData.password);
  console.log('测试聊天 ID:', testData.chatId);
}

// 运行测试
runTests().catch(console.error);
