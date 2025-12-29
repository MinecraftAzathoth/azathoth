<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white dark:bg-gray-800 shadow-sm">
      <nav class="container mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <!-- Logo -->
          <NuxtLink to="/" class="flex items-center space-x-2">
            <img src="/logo.svg" alt="Azathoth" class="h-8 w-8" />
            <span class="text-xl font-bold text-gray-900 dark:text-white">Azathoth</span>
          </NuxtLink>

          <!-- Desktop Navigation -->
          <div class="hidden md:flex items-center space-x-6">
            <NuxtLink
              v-for="item in navItems"
              :key="item.to"
              :to="item.to"
              class="text-gray-600 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400 transition-colors"
            >
              {{ item.label }}
            </NuxtLink>
          </div>

          <!-- Right Side -->
          <div class="flex items-center space-x-4">
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

            <!-- Language Switcher -->
            <div class="relative">
              <button
                @click="showLangMenu = !showLangMenu"
                class="p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
                </svg>
              </button>
              <div
                v-if="showLangMenu"
                class="absolute right-0 mt-2 w-32 bg-white dark:bg-gray-800 rounded-md shadow-lg py-1 z-50"
              >
                <button
                  v-for="locale in locales"
                  :key="locale.code"
                  @click="switchLocale(locale.code)"
                  class="block w-full px-4 py-2 text-left text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  {{ locale.name }}
                </button>
              </div>
            </div>

            <!-- Mobile Menu Button -->
            <button
              @click="showMobileMenu = !showMobileMenu"
              class="md:hidden p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  v-if="!showMobileMenu"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 6h16M4 12h16M4 18h16"
                />
                <path
                  v-else
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
        </div>
      </nav>
    </header>

    <!-- Main Content with Sidebar -->
    <div class="flex">
      <!-- Sidebar -->
      <aside
        class="hidden lg:block w-64 flex-shrink-0 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 min-h-[calc(100vh-64px)] sticky top-16"
      >
        <nav class="p-4 space-y-2">
          <div class="mb-4">
            <h3 class="text-sm font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              API 文档
            </h3>
          </div>
          <NuxtLink
            v-for="item in sidebarItems"
            :key="item.to"
            :to="item.to"
            class="block px-3 py-2 rounded-md text-sm transition-colors"
            :class="isActiveRoute(item.to)
              ? 'bg-primary-100 dark:bg-primary-900 text-primary-700 dark:text-primary-300 font-medium'
              : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
          >
            {{ item.label }}
          </NuxtLink>
        </nav>
      </aside>

      <!-- Mobile Sidebar -->
      <div
        v-if="showMobileSidebar"
        class="lg:hidden fixed inset-0 z-40 bg-black/50"
        @click="showMobileSidebar = false"
      >
        <aside
          class="w-64 bg-white dark:bg-gray-800 min-h-full"
          @click.stop
        >
          <nav class="p-4 space-y-2">
            <div class="mb-4 flex justify-between items-center">
              <h3 class="text-sm font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                API 文档
              </h3>
              <button @click="showMobileSidebar = false" class="text-gray-500">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <NuxtLink
              v-for="item in sidebarItems"
              :key="item.to"
              :to="item.to"
              class="block px-3 py-2 rounded-md text-sm transition-colors"
              :class="isActiveRoute(item.to)
                ? 'bg-primary-100 dark:bg-primary-900 text-primary-700 dark:text-primary-300 font-medium'
                : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
              @click="showMobileSidebar = false"
            >
              {{ item.label }}
            </NuxtLink>
          </nav>
        </aside>
      </div>

      <!-- Page Content -->
      <main class="flex-1 min-w-0">
        <!-- Mobile Sidebar Toggle -->
        <button
          @click="showMobileSidebar = true"
          class="lg:hidden fixed bottom-4 right-4 z-30 p-3 bg-primary-600 text-white rounded-full shadow-lg hover:bg-primary-700"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>

        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
const { t, locale, locales, setLocale } = useI18n()
const colorMode = useColorMode()
const route = useRoute()

const isDark = computed(() => colorMode.value === 'dark')

const showMobileMenu = ref(false)
const showLangMenu = ref(false)
const showMobileSidebar = ref(false)

const navItems = computed(() => [
  { to: '/market', label: t('nav.market') },
  { to: '/generator', label: t('nav.generator') },
  { to: '/forum', label: t('nav.forum') },
  { to: '/wiki', label: t('nav.docs') },
])

const sidebarItems = [
  { to: '/wiki/api', label: '概述' },
  { to: '/wiki/api/auth', label: '认证模块' },
  { to: '/wiki/api/market', label: '市场模块' },
  { to: '/wiki/api/payment', label: '支付模块' },
  { to: '/wiki/api/generator', label: '生成器模块' },
  { to: '/wiki/api/forum', label: '论坛模块' },
  { to: '/wiki/api/review', label: '审核模块' },
]

const toggleDarkMode = () => {
  colorMode.preference = isDark.value ? 'light' : 'dark'
}

const switchLocale = (code: string) => {
  setLocale(code)
  showLangMenu.value = false
}

const isActiveRoute = (path: string) => {
  return route.path === path || route.path === path + '/'
}

// Close menus on click outside
onMounted(() => {
  document.addEventListener('click', (e) => {
    const target = e.target as HTMLElement
    if (!target.closest('[data-menu]')) {
      showLangMenu.value = false
    }
  })
})
</script>
