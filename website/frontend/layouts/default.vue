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
            <!-- Search -->
            <button
              @click="showSearch = true"
              class="p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </button>

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
                class="absolute right-0 mt-2 w-32 bg-white dark:bg-gray-800 rounded-md shadow-lg py-1"
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

            <!-- User Menu / Login -->
            <div v-if="user" class="relative">
              <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center space-x-2"
              >
                <img
                  :src="user.avatarUrl || '/default-avatar.png'"
                  :alt="user.username"
                  class="w-8 h-8 rounded-full"
                />
              </button>
              <div
                v-if="showUserMenu"
                class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-md shadow-lg py-1"
              >
                <NuxtLink
                  to="/user/profile"
                  class="block px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  {{ $t('nav.profile') }}
                </NuxtLink>
                <NuxtLink
                  to="/user/resources"
                  class="block px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  {{ $t('nav.myResources') }}
                </NuxtLink>
                <NuxtLink
                  to="/user/wallet"
                  class="block px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  {{ $t('nav.wallet') }}
                </NuxtLink>
                <hr class="my-1 border-gray-200 dark:border-gray-700" />
                <button
                  @click="logout"
                  class="block w-full px-4 py-2 text-left text-red-600 hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  {{ $t('nav.logout') }}
                </button>
              </div>
            </div>
            <NuxtLink
              v-else
              to="/auth/login"
              class="btn-primary"
            >
              {{ $t('nav.login') }}
            </NuxtLink>

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

        <!-- Mobile Navigation -->
        <div v-if="showMobileMenu" class="md:hidden mt-4 pb-4">
          <NuxtLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="block py-2 text-gray-600 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400"
            @click="showMobileMenu = false"
          >
            {{ item.label }}
          </NuxtLink>
        </div>
      </nav>
    </header>

    <!-- Main Content -->
    <main class="flex-1">
      <slot />
    </main>

    <!-- Footer -->
    <footer class="bg-gray-800 text-gray-300 py-12">
      <div class="container mx-auto px-4">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <h3 class="text-white font-bold mb-4">Azathoth</h3>
            <p class="text-sm">
              {{ $t('footer.description') }}
            </p>
          </div>
          <div>
            <h4 class="text-white font-semibold mb-4">{{ $t('footer.product') }}</h4>
            <ul class="space-y-2 text-sm">
              <li><NuxtLink to="/market" class="hover:text-white">{{ $t('nav.market') }}</NuxtLink></li>
              <li><NuxtLink to="/generator" class="hover:text-white">{{ $t('nav.generator') }}</NuxtLink></li>
              <li><NuxtLink to="/wiki" class="hover:text-white">{{ $t('nav.docs') }}</NuxtLink></li>
            </ul>
          </div>
          <div>
            <h4 class="text-white font-semibold mb-4">{{ $t('footer.community') }}</h4>
            <ul class="space-y-2 text-sm">
              <li><NuxtLink to="/forum" class="hover:text-white">{{ $t('nav.forum') }}</NuxtLink></li>
              <li><a href="https://github.com/azathoth" target="_blank" class="hover:text-white">GitHub</a></li>
              <li><a href="https://discord.gg/azathoth" target="_blank" class="hover:text-white">Discord</a></li>
            </ul>
          </div>
          <div>
            <h4 class="text-white font-semibold mb-4">{{ $t('footer.support') }}</h4>
            <ul class="space-y-2 text-sm">
              <li><NuxtLink to="/about" class="hover:text-white">{{ $t('footer.about') }}</NuxtLink></li>
              <li><NuxtLink to="/terms" class="hover:text-white">{{ $t('footer.terms') }}</NuxtLink></li>
              <li><NuxtLink to="/privacy" class="hover:text-white">{{ $t('footer.privacy') }}</NuxtLink></li>
            </ul>
          </div>
        </div>
        <div class="mt-8 pt-8 border-t border-gray-700 text-center text-sm">
          <p>&copy; {{ new Date().getFullYear() }} Azathoth. All rights reserved.</p>
        </div>
      </div>
    </footer>

    <!-- Search Modal -->
    <Teleport to="body">
      <div
        v-if="showSearch"
        class="fixed inset-0 z-50 flex items-start justify-center pt-20 bg-black/50"
        @click.self="showSearch = false"
      >
        <div class="w-full max-w-2xl bg-white dark:bg-gray-800 rounded-lg shadow-xl p-4">
          <input
            type="text"
            v-model="searchQuery"
            :placeholder="$t('search.placeholder')"
            class="w-full px-4 py-3 text-lg border-0 focus:ring-0 bg-transparent text-gray-900 dark:text-white"
            autofocus
            @keyup.enter="performSearch"
            @keyup.esc="showSearch = false"
          />
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
const { t, locale, locales, setLocale } = useI18n()
const colorMode = useColorMode()
const router = useRouter()
const authStore = useAuthStore()

const user = computed(() => authStore.user)
const isDark = computed(() => colorMode.value === 'dark')

const showMobileMenu = ref(false)
const showUserMenu = ref(false)
const showLangMenu = ref(false)
const showSearch = ref(false)
const searchQuery = ref('')

const navItems = computed(() => [
  { to: '/market', label: t('nav.market') },
  { to: '/generator', label: t('nav.generator') },
  { to: '/forum', label: t('nav.forum') },
  { to: '/wiki', label: t('nav.docs') },
])

const toggleDarkMode = () => {
  colorMode.preference = isDark.value ? 'light' : 'dark'
}

const switchLocale = (code: string) => {
  setLocale(code)
  showLangMenu.value = false
}

const logout = async () => {
  await authStore.logout()
  showUserMenu.value = false
  router.push('/')
}

const performSearch = () => {
  if (searchQuery.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(searchQuery.value)}`)
    showSearch.value = false
    searchQuery.value = ''
  }
}

// Close menus on click outside
onMounted(() => {
  document.addEventListener('click', (e) => {
    const target = e.target as HTMLElement
    if (!target.closest('[data-menu]')) {
      showUserMenu.value = false
      showLangMenu.value = false
    }
  })
})
</script>

<style>
.btn-primary {
  @apply px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 transition-colors;
}
</style>
