<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
      <nav class="container mx-auto px-4 py-3">
        <div class="flex items-center justify-between">
          <!-- Logo -->
          <div class="flex items-center space-x-4">
            <NuxtLink to="/" class="flex items-center space-x-2">
              <img src="/logo.svg" alt="Azathoth" class="h-8 w-8" />
              <span class="text-xl font-bold text-gray-900 dark:text-white">Azathoth</span>
            </NuxtLink>
            <span class="text-gray-300 dark:text-gray-600">|</span>
            <NuxtLink to="/docs" class="text-gray-600 dark:text-gray-400 hover:text-primary-600 font-medium">
              开发者文档
            </NuxtLink>
          </div>

          <!-- Search -->
          <div class="hidden md:flex flex-1 max-w-md mx-8">
            <div class="relative w-full">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索文档..."
                class="w-full px-4 py-2 pl-10 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500"
                @keyup.enter="performSearch"
              />
              <svg
                class="absolute left-3 top-2.5 w-5 h-5 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
          </div>

          <!-- Right Side -->
          <div class="flex items-center space-x-4">
            <!-- Version Selector -->
            <select
              v-model="selectedVersion"
              class="px-3 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            >
              <option value="latest">v1.0.0 (最新)</option>
              <option value="0.9">v0.9.x</option>
              <option value="0.8">v0.8.x</option>
            </select>

            <!-- Theme Toggle -->
            <button
              @click="toggleDarkMode"
              class="p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
            >
              <svg v-if="isDark" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
              <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
              </svg>
            </button>

            <!-- GitHub Link -->
            <a
              href="https://github.com/azathoth-mc/azathoth"
              target="_blank"
              class="p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
            >
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
            </a>

            <!-- Mobile Menu -->
            <button
              @click="showMobileSidebar = !showMobileSidebar"
              class="lg:hidden p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
          </div>
        </div>
      </nav>
    </header>

    <!-- Main Content -->
    <div class="flex">
      <!-- Sidebar -->
      <aside
        class="hidden lg:block w-72 flex-shrink-0 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 min-h-[calc(100vh-57px)] sticky top-[57px] overflow-y-auto"
      >
        <nav class="p-4">
          <template v-for="section in sidebarSections" :key="section.title">
            <h3 class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2 mt-6 first:mt-0">
              {{ section.title }}
            </h3>
            <ul class="space-y-1">
              <li v-for="item in section.items" :key="item.to">
                <NuxtLink
                  :to="item.to"
                  class="block px-3 py-2 rounded-md text-sm transition-colors"
                  :class="isActiveRoute(item.to)
                    ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300 font-medium'
                    : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
                >
                  {{ item.label }}
                </NuxtLink>
              </li>
            </ul>
          </template>
        </nav>
      </aside>

      <!-- Mobile Sidebar -->
      <div
        v-if="showMobileSidebar"
        class="lg:hidden fixed inset-0 z-40 bg-black/50"
        @click="showMobileSidebar = false"
      >
        <aside
          class="w-72 bg-white dark:bg-gray-800 min-h-full overflow-y-auto"
          @click.stop
        >
          <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
            <span class="font-semibold text-gray-900 dark:text-white">文档导航</span>
            <button @click="showMobileSidebar = false" class="text-gray-500">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <nav class="p-4">
            <template v-for="section in sidebarSections" :key="section.title">
              <h3 class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2 mt-6 first:mt-0">
                {{ section.title }}
              </h3>
              <ul class="space-y-1">
                <li v-for="item in section.items" :key="item.to">
                  <NuxtLink
                    :to="item.to"
                    class="block px-3 py-2 rounded-md text-sm transition-colors"
                    :class="isActiveRoute(item.to)
                      ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300 font-medium'
                      : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
                    @click="showMobileSidebar = false"
                  >
                    {{ item.label }}
                  </NuxtLink>
                </li>
              </ul>
            </template>
          </nav>
        </aside>
      </div>

      <!-- Content Area -->
      <main class="flex-1 min-w-0">
        <div class="max-w-4xl mx-auto px-6 py-8">
          <slot />
        </div>
      </main>

      <!-- Table of Contents -->
      <aside
        class="hidden xl:block w-64 flex-shrink-0 sticky top-[57px] h-[calc(100vh-57px)] overflow-y-auto p-4"
      >
        <div v-if="toc.length" class="text-sm">
          <h4 class="font-semibold text-gray-900 dark:text-white mb-3">本页目录</h4>
          <ul class="space-y-2">
            <li v-for="item in toc" :key="item.id">
              <a
                :href="`#${item.id}`"
                class="block text-gray-600 dark:text-gray-400 hover:text-primary-600 dark:hover:text-primary-400 transition-colors"
                :class="{ 'pl-4': item.depth === 3 }"
              >
                {{ item.text }}
              </a>
            </li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const router = useRouter()
const colorMode = useColorMode()

const isDark = computed(() => colorMode.value === 'dark')
const showMobileSidebar = ref(false)
const searchQuery = ref('')
const selectedVersion = ref('latest')
const toc = ref<{ id: string; text: string; depth: number }[]>([])

const sidebarSections = [
  {
    title: '入门',
    items: [
      { to: '/docs', label: '简介' },
      { to: '/docs/getting-started', label: '快速开始' },
      { to: '/docs/installation', label: '安装指南' },
      { to: '/docs/project-structure', label: '项目结构' },
    ],
  },
  {
    title: '核心概念',
    items: [
      { to: '/docs/core/architecture', label: '架构设计' },
      { to: '/docs/core/lifecycle', label: '生命周期' },
      { to: '/docs/core/dependency-injection', label: '依赖注入' },
      { to: '/docs/core/configuration', label: '配置系统' },
      { to: '/docs/core/events', label: '事件系统' },
    ],
  },
  {
    title: '游戏系统',
    items: [
      { to: '/docs/systems/skill', label: '技能系统' },
      { to: '/docs/systems/dungeon', label: '副本系统' },
      { to: '/docs/systems/quest', label: '任务系统' },
      { to: '/docs/systems/item', label: '物品系统' },
      { to: '/docs/systems/npc', label: 'NPC 系统' },
      { to: '/docs/systems/ai', label: 'AI 行为树' },
    ],
  },
  {
    title: '网络通信',
    items: [
      { to: '/docs/network/protocol', label: '协议设计' },
      { to: '/docs/network/grpc', label: 'gRPC 服务' },
      { to: '/docs/network/websocket', label: 'WebSocket' },
      { to: '/docs/network/packet', label: '数据包处理' },
    ],
  },
  {
    title: '数据存储',
    items: [
      { to: '/docs/storage/database', label: '数据库集成' },
      { to: '/docs/storage/redis', label: 'Redis 缓存' },
      { to: '/docs/storage/serialization', label: '序列化' },
    ],
  },
  {
    title: 'SDK 工具',
    items: [
      { to: '/docs/sdk/gradle-plugin', label: 'Gradle 插件' },
      { to: '/docs/sdk/cli', label: '命令行工具' },
      { to: '/docs/sdk/generator', label: '项目生成器' },
    ],
  },
  {
    title: '进阶',
    items: [
      { to: '/docs/advanced/testing', label: '测试指南' },
      { to: '/docs/advanced/performance', label: '性能优化' },
      { to: '/docs/advanced/debugging', label: '调试技巧' },
      { to: '/docs/advanced/deployment', label: '部署指南' },
    ],
  },
  {
    title: 'API 参考',
    items: [
      { to: '/docs/api/overview', label: 'API 概览' },
      { to: '/docs/api/annotations', label: '注解参考' },
      { to: '/docs/api/interfaces', label: '接口参考' },
    ],
  },
]

const toggleDarkMode = () => {
  colorMode.preference = isDark.value ? 'light' : 'dark'
}

const isActiveRoute = (path: string) => {
  return route.path === path || route.path === path + '/'
}

const performSearch = () => {
  if (searchQuery.value.trim()) {
    router.push(`/docs/search?q=${encodeURIComponent(searchQuery.value)}`)
  }
}

// Extract TOC from page content
onMounted(() => {
  // This would be populated by @nuxt/content or similar
  // For now, leave it empty - the slot content can provide TOC
})
</script>
