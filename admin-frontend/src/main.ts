import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import type { RouteRecordRaw } from 'vue-router'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('./views/Dashboard.vue'),
    meta: { title: '仪表盘', icon: 'dashboard' }
  },
  {
    path: '/players',
    name: 'Players',
    component: () => import('./views/players/PlayerList.vue'),
    meta: { title: '玩家管理', icon: 'users' }
  },
  {
    path: '/players/:id',
    name: 'PlayerDetail',
    component: () => import('./views/players/PlayerDetail.vue'),
    meta: { title: '玩家详情', hidden: true }
  },
  {
    path: '/instances',
    name: 'Instances',
    component: () => import('./views/instances/InstanceList.vue'),
    meta: { title: '实例管理', icon: 'server' }
  },
  {
    path: '/activities',
    name: 'Activities',
    component: () => import('./views/activities/ActivityList.vue'),
    meta: { title: '活动管理', icon: 'calendar' }
  },
  {
    path: '/announcements',
    name: 'Announcements',
    component: () => import('./views/announcements/AnnouncementList.vue'),
    meta: { title: '公告管理', icon: 'megaphone' }
  },
  {
    path: '/config',
    name: 'Config',
    component: () => import('./views/config/ConfigEditor.vue'),
    meta: { title: '配置管理', icon: 'settings' }
  },
  {
    path: '/logs',
    name: 'Logs',
    component: () => import('./views/logs/LogViewer.vue'),
    meta: { title: '日志查看', icon: 'file-text' }
  },
  {
    path: '/analytics',
    name: 'Analytics',
    component: () => import('./views/analytics/AnalyticsDashboard.vue'),
    meta: { title: '数据分析', icon: 'bar-chart' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('./views/auth/Login.vue'),
    meta: { title: '登录', layout: 'blank' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 创建应用
const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 导航守卫：未认证时重定向到登录页
router.beforeEach(async (to, _from, next) => {
  if (to.name === 'Login') {
    next()
    return
  }

  const { useAuthStore } = await import('./stores/auth')
  const authStore = useAuthStore()

  if (authStore.isAuthenticated) {
    next()
  } else {
    const ok = await authStore.checkAuth()
    if (ok) {
      next()
    } else {
      next({ name: 'Login', query: { redirect: to.fullPath } })
    }
  }
})

app.mount('#app')

export { router }
