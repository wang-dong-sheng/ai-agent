import { createApp } from 'vue'
import App from './App.vue'
import './style.css'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { useAppStore } from './store/app'

// 创建应用实例
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 创建并使用Pinia
const pinia = createPinia()
app.use(pinia)

// 使用Element Plus和路由
app.use(ElementPlus)
app.use(router)

// 初始化应用状态
const appStore = useAppStore()
appStore.initDarkMode()

// 挂载应用
app.mount('#app')