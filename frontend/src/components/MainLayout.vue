<template>
  <div class="main-layout" :class="{ 'dark-mode': isDarkMode }">
    <!-- 左侧边栏 -->
    <Sidebar 
      :collapsed="isSidebarCollapsed" 
      @toggle="isSidebarCollapsed = !isSidebarCollapsed"
    />
    
    <!-- 右侧主内容区 -->
    <div class="main-content" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
      <!-- 移动端侧边栏开关 -->
      <div class="mobile-sidebar-toggle" @click="isSidebarCollapsed = !isSidebarCollapsed">
        <el-icon><Menu /></el-icon>
      </div>
      
      <!-- 内容区域 -->
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '../store/app'
import Sidebar from './Sidebar.vue'
import { Menu } from '@element-plus/icons-vue'

const route = useRoute()
const store = useAppStore()

// 侧边栏折叠状态
const isSidebarCollapsed = ref(false)

// 深色模式状态
const isDarkMode = computed(() => store.darkMode)

// 监听路由变化，在移动端自动折叠侧边栏
onMounted(() => {
  const handleResize = () => {
    if (window.innerWidth <= 768) {
      isSidebarCollapsed.value = true
    } else {
      isSidebarCollapsed.value = false
    }
  }
  
  handleResize()
  window.addEventListener('resize', handleResize)
  
  // 清理事件监听器
  return () => {
    window.removeEventListener('resize', handleResize)
  }
})
</script>

<style lang="scss" scoped>
.main-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  
  &.dark-mode {
    background-color: #1a1a1a;
  }
  
  .main-content {
    flex: 1;
    height: 100vh;
    overflow: hidden;
    position: relative;
    transition: all 0.3s ease;
    
    &.sidebar-collapsed {
      margin-left: 0;
    }
    
    .mobile-sidebar-toggle {
      position: fixed;
      top: 16px;
      left: 16px;
      z-index: 1000;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background-color: #fff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      @media (min-width: 769px) {
        display: none;
      }
      
      .dark-mode & {
        background-color: #333;
        color: #fff;
      }
    }
  }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>