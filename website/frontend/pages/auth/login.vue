<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4">
    <div class="max-w-md w-full">
      <div class="text-center mb-8">
        <NuxtLink to="/" class="inline-block">
          <img src="/logo.svg" alt="Azathoth" class="h-12 mx-auto" />
        </NuxtLink>
        <h1 class="mt-6 text-3xl font-bold text-gray-900 dark:text-white">
          {{ $t('auth.login.title') }}
        </h1>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-8">
        <form @submit.prevent="handleLogin" class="space-y-6">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              {{ $t('auth.login.username') }}
            </label>
            <input
              v-model="username"
              type="text"
              required
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              {{ $t('auth.login.password') }}
            </label>
            <input
              v-model="password"
              type="password"
              required
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500"
            />
          </div>

          <div class="flex items-center justify-between">
            <label class="flex items-center">
              <input
                v-model="remember"
                type="checkbox"
                class="w-4 h-4 text-primary-600 rounded"
              />
              <span class="ml-2 text-sm text-gray-600 dark:text-gray-400">
                {{ $t('auth.login.remember') }}
              </span>
            </label>
            <NuxtLink
              to="/auth/forgot-password"
              class="text-sm text-primary-600 hover:underline"
            >
              {{ $t('auth.login.forgot') }}
            </NuxtLink>
          </div>

          <div v-if="error" class="p-3 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-lg text-sm">
            {{ error }}
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-primary-600 text-white rounded-lg font-semibold hover:bg-primary-700 disabled:opacity-50 transition-colors"
          >
            <span v-if="loading">{{ $t('common.loading') }}</span>
            <span v-else>{{ $t('auth.login.submit') }}</span>
          </button>
        </form>

        <p class="mt-6 text-center text-sm text-gray-600 dark:text-gray-400">
          {{ $t('auth.login.noAccount') }}
          <NuxtLink to="/auth/register" class="text-primary-600 hover:underline">
            {{ $t('auth.login.register') }}
          </NuxtLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: false
})

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const remember = ref(false)
const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  error.value = ''
  loading.value = true

  const result = await authStore.login(username.value, password.value, remember.value)

  if (result.success) {
    const redirect = route.query.redirect as string || '/'
    router.push(redirect)
  } else {
    error.value = result.error || '登录失败'
  }

  loading.value = false
}

useHead({
  title: t('auth.login.title') + ' - Azathoth',
})
</script>
