<template>
  <div class="register-container" :class="{ 'dark-mode': isDarkMode }">
    <div class="register-card">
      <div class="register-header">
        <h2>AI助手</h2>
        <p>创建新账号，开始智能对话</p>
      </div>
      
      <el-form 
        ref="registerFormRef" 
        :model="registerForm" 
        :rules="registerRules" 
        label-width="0"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="用户名" 
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="密码" 
            prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="确认密码" 
            prefix-icon="Check"
            show-password
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="nickname">
          <el-input 
            v-model="registerForm.nickname" 
            placeholder="昵称（可选）" 
            prefix-icon="Avatar"
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="handleRegister" 
            size="large"
            class="register-button"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
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
const registerFormRef = ref(null)
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})
const loading = ref(false)

// 深色模式状态
const isDarkMode = computed(() => appStore.darkMode)

// 表单校验规则
const registerRules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        // 密码强度校验
        if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{6,}$/.test(value)) {
          callback(new Error('密码至少包含大小写字母和数字'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  nickname: [
    { max: 20, message: '昵称长度不超过20位', trigger: 'blur' }
  ]
})

// 注册处理
const handleRegister = async () => {
  // 表单校验
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    // 调用注册接口
    await userStore.register({
      username: registerForm.value.username,
      password: registerForm.value.password,
      nickname: registerForm.value.nickname
    })
    
    // 显示成功提示
    ElMessage.success('注册成功，即将跳转到登录页面')
    
    // 注册成功后跳转到登录页面
    setTimeout(() => {
      router.push('/login')
    }, 1000)
  } catch (error) {
    console.error('注册失败:', error)
    // 显示错误提示
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 初始化
appStore.initDarkMode()
</script>

<style lang="scss" scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  
  &.dark-mode {
    background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  }
  
  .register-card {
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
    
    .register-header {
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
    
    .register-form {
      margin-bottom: 24px;
      
      .el-form-item {
        margin-bottom: 20px;
      }
      
      .register-button {
        width: 100%;
        height: 48px;
        border-radius: 24px;
        font-size: 16px;
      }
    }
    
    .register-footer {
      text-align: center;
      color: #909399;
      
      .dark-mode & {
        color: #909090;
      }
      
      .login-link {
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
