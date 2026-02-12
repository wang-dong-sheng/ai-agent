import { createRouter, createWebHashHistory } from 'vue-router'
import LoginPage from '../views/LoginPage.vue'
import RegisterPage from '../views/RegisterPage.vue'
import MainLayout from '../components/MainLayout.vue'
import ChatView from '../components/ChatView.vue'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterPage,
    meta: { requiresAuth: false }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'ChatHome',
        component: ChatView,
        props: { conversationId: '' }
      },
      {
        path: ':id',
        name: 'ChatDetail',
        component: ChatView,
        props: route => ({ conversationId: route.params.id })
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 全局导航守卫，检查登录状态
router.beforeEach((to, from, next) => {
  // 初始化用户状态
  const userStore = useUserStore()
  userStore.initUser()
  
  // 检查是否需要认证
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)
  
  if (requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
