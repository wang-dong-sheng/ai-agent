import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    // 深色模式状态
    darkMode: false
  }),
  
  actions: {
    // 切换深色模式
    toggleDarkMode() {
      this.darkMode = !this.darkMode
      // 保存到本地存储
      localStorage.setItem('darkMode', this.darkMode.toString())
      // 应用到DOM
      if (this.darkMode) {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    },
    
    // 初始化深色模式
    initDarkMode() {
      const savedMode = localStorage.getItem('darkMode')
      if (savedMode !== null) {
        this.darkMode = savedMode === 'true'
      } else {
        // 跟随系统
        this.darkMode = window.matchMedia('(prefers-color-scheme: dark)').matches
      }
      // 应用到DOM
      if (this.darkMode) {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    }
  }
})