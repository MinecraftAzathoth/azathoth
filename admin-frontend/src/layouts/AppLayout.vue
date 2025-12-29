<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const sidebarCollapsed = ref(false)

const menuItems = [
  { path: '/', name: '仪表盘', icon: 'dashboard' },
  { path: '/players', name: '玩家管理', icon: 'users' },
  { path: '/instances', name: '实例管理', icon: 'server' },
  { path: '/activities', name: '活动管理', icon: 'calendar' },
  { path: '/announcements', name: '公告管理', icon: 'megaphone' },
  { path: '/config', name: '配置管理', icon: 'settings' },
  { path: '/logs', name: '日志查看', icon: 'file-text' },
  { path: '/analytics', name: '数据分析', icon: 'bar-chart' }
]

const currentPath = computed(() => route.path)

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-layout">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <span v-if="!sidebarCollapsed">Azathoth Admin</span>
          <span v-else>A</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: currentPath === item.path }"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span v-if="!sidebarCollapsed" class="nav-text">{{ item.name }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <button @click="toggleSidebar" class="toggle-btn">
          {{ sidebarCollapsed ? '→' : '←' }}
        </button>
      </div>
    </aside>

    <!-- Main Content -->
    <div class="main-container">
      <!-- Header -->
      <header class="app-header">
        <div class="header-left">
          <h1 class="page-title">{{ route.meta.title }}</h1>
        </div>
        <div class="header-right">
          <div class="user-info" v-if="authStore.user">
            <span class="username">{{ authStore.user.username }}</span>
            <button @click="handleLogout" class="logout-btn">登出</button>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <main class="page-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 240px;
  background: var(--sidebar-bg);
  color: white;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  font-size: 1.25rem;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.nav-item.active {
  background: var(--primary-color);
  color: white;
}

.nav-icon {
  width: 24px;
  margin-right: 12px;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.toggle-btn {
  width: 100%;
  padding: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: white;
  cursor: pointer;
  border-radius: 4px;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.app-header {
  height: 64px;
  background: white;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.page-title {
  font-size: 1.25rem;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logout-btn {
  padding: 6px 12px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  cursor: pointer;
}

.logout-btn:hover {
  background: var(--bg-color);
}

.page-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
