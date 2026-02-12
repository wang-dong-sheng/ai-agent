<template>
  <div class="login-container" :class="{ 'dark-mode': isDarkMode }">
    <div class="login-card">
      <div class="login-header">
        <h2>AI助手</h2>
        <p>欢迎回来，请登录您的账号</p>
      </div>
      
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        label-width="0"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="用户名" 
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码" 
            prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="loginForm.remember">记住密码（7天免登录）</el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="handleLogin" 
            size="large"
            class="login-button"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="register-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { useAppStore } from '../store/app'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 表单状态
const loginFormRef = ref(null)
const loginForm = ref({
  username: '',
  password: '',
  remember: false
})
const loading = ref(false)

// 深色模式状态
const isDarkMode = computed(() => appStore.darkMode)

// 表单校验规则
const loginRules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
})

// 登录处理
const handleLogin = async () => {
  // 表单校验
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    // 调用登录接口
    await userStore.login({
      username: loginForm.value.username,
      password: loginForm.value.password
    })
    
    // 跳转到主页面
    router.push('/chat')
  } catch (error) {
    console.error('登录失败:', error)
    // 显示错误提示
    ElMessage.error(error.message || '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 初始化
appStore.initDarkMode()
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  
  &.dark-mode {
    background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  }
  
  .login-card {
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    padding: 40px;
    width: 100%;
    max-width: 420px;
    
    .dark-mode & {
      background: #1f1f1f;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    }
    
    .login-header {
      text-align: center;
      margin-bottom: 32px;
      
      h2 {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: bold;
        color: #409eff;
      }
      
      p {
        margin: 0;
        color: #909399;
        
        .dark-mode & {
          color: #909090;
        }
      }
    }
    
    .login-form {
      margin-bottom: 24px;
      
      .el-form-item {
        margin-bottom: 20px;
      }
      
      .login-button {
        width: 100%;
        height: 48px;
        border-radius: 24px;
        font-size: 16px;
      }
    }
    
    .login-footer {
      text-align: center;
      color: #909399;
      
      .dark-mode & {
        color: #909090;
      }
      
      .register-link {
        color: #409eff;
        text-decoration: none;
        margin-left: 8px;
        
        &:hover {
          text-decoration: underline;
        }
      }
    }
  }
}
</style>
