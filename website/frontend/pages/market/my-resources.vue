<template>
  <div class="container mx-auto px-4 py-8">
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white">
        我的资源
      </h1>
      <NuxtLink
        to="/market/publish"
        class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
      >
        发布新资源
      </NuxtLink>
    </div>

    <!-- 未登录提示 -->
    <div
      v-if="!authStore.isAuthenticated"
      class="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-6 text-center"
    >
      <p class="text-yellow-800 dark:text-yellow-200 mb-4">请先登录后查看您的资源</p>
      <NuxtLink
        to="/auth/login"
        class="inline-block px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
      >
        前往登录
      </NuxtLink>
    </div>

    <template v-else>
      <!-- Loading State -->
      <div v-if="marketStore.loading" class="text-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-500 dark:text-gray-400">加载中...</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="!marketStore.myResources.length" class="text-center py-20">
        <div class="text-gray-400 dark:text-gray-500 mb-4">
          <svg
            class="w-16 h-16 mx-auto"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
            />
          </svg>
        </div>
        <p class="text-gray-500 dark:text-gray-400 mb-4">您还没有发布任何资源</p>
        <NuxtLink
          to="/market/publish"
          class="inline-block px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
        >
          发布第一个资源
        </NuxtLink>
      </div>

      <!-- Resources List -->
      <div v-else class="space-y-4">
        <div
          v-for="resource in marketStore.myResources"
          :key="resource.resourceId"
          class="bg-white dark:bg-gray-800 rounded-lg shadow p-6"
        >
          <div class="flex items-start gap-4">
            <!-- Icon -->
            <div class="w-16 h-16 bg-gray-100 dark:bg-gray-700 rounded-lg flex items-center justify-center flex-shrink-0">
              <img
                v-if="resource.icon"
                :src="resource.icon"
                :alt="resource.name"
                class="w-12 h-12 object-cover rounded"
              />
              <svg
                v-else
                class="w-8 h-8 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
                />
              </svg>
            </div>

            <!-- Info -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <NuxtLink
                  :to="`/market/${resource.slug}`"
                  class="text-lg font-semibold text-gray-900 dark:text-white hover:text-primary-600"
                >
                  {{ resource.name }}
                </NuxtLink>
                <span :class="statusClasses[resource.status]" class="px-2 py-0.5 text-xs rounded-full">
                  {{ statusLabels[resource.status] }}
                </span>
              </div>

              <p class="text-gray-500 dark:text-gray-400 text-sm line-clamp-2 mb-2">
                {{ resource.description }}
              </p>

              <div class="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
                <span>{{ typeLabels[resource.type] }}</span>
                <span>v{{ resource.latestVersion }}</span>
                <span>{{ resource.downloads }} 下载</span>
                <span v-if="resource.rating > 0">{{ resource.rating.toFixed(1) }} 评分</span>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex items-center gap-2 flex-shrink-0">
              <button
                @click="openVersionDialog(resource)"
                class="px-3 py-1.5 text-sm bg-green-600 text-white rounded hover:bg-green-700"
              >
                发布版本
              </button>
              <NuxtLink
                :to="`/market/edit/${resource.resourceId}`"
                class="px-3 py-1.5 text-sm bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-gray-200 rounded hover:bg-gray-300 dark:hover:bg-gray-500"
              >
                编辑
              </NuxtLink>
              <button
                @click="confirmDelete(resource)"
                class="px-3 py-1.5 text-sm bg-red-600 text-white rounded hover:bg-red-700"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Version Dialog -->
    <div
      v-if="showVersionDialog"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="showVersionDialog = false"
    >
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl p-6 w-full max-w-md mx-4">
        <h3 class="text-xl font-semibold mb-4 text-gray-900 dark:text-white">
          发布新版本 - {{ selectedResource?.name }}
        </h3>

        <form @submit.prevent="handlePublishVersion" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              版本号 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="versionForm.version"
              type="text"
              required
              placeholder="例如: 1.0.0"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              更新日志 <span class="text-red-500">*</span>
            </label>
            <textarea
              v-model="versionForm.changelog"
              required
              rows="4"
              placeholder="描述此版本的更新内容"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              最低 API 版本 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="versionForm.minApiVersion"
              type="text"
              required
              placeholder="例如: 1.0.0"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              上传文件 <span class="text-red-500">*</span>
            </label>
            <input
              ref="fileInput"
              type="file"
              required
              accept=".jar,.zip"
              @change="handleFileSelect"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            />
            <p class="text-sm text-gray-500 mt-1">支持 .jar 或 .zip 文件</p>
          </div>

          <div v-if="versionError" class="text-red-600 text-sm">{{ versionError }}</div>

          <div class="flex justify-end gap-3 pt-4">
            <button
              type="button"
              @click="showVersionDialog = false"
              class="px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="marketStore.submitting"
              class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
            >
              {{ marketStore.submitting ? '发布中...' : '发布' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Confirm Dialog -->
    <div
      v-if="showDeleteDialog"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="showDeleteDialog = false"
    >
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl p-6 w-full max-w-md mx-4">
        <h3 class="text-xl font-semibold mb-4 text-gray-900 dark:text-white">
          确认删除
        </h3>
        <p class="text-gray-600 dark:text-gray-400 mb-6">
          确定要删除资源「{{ selectedResource?.name }}」吗？此操作无法撤销。
        </p>
        <div class="flex justify-end gap-3">
          <button
            @click="showDeleteDialog = false"
            class="px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
          >
            取消
          </button>
          <button
            @click="handleDelete"
            :disabled="marketStore.submitting"
            class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50"
          >
            {{ marketStore.submitting ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { MarketResource } from '~/types'

const authStore = useAuthStore()
const marketStore = useMarketStore()

const selectedResource = ref<MarketResource | null>(null)
const showVersionDialog = ref(false)
const showDeleteDialog = ref(false)

const versionForm = reactive({
  version: '',
  changelog: '',
  minApiVersion: '1.0.0',
  file: null as File | null,
})
const versionError = ref('')
const fileInput = ref<HTMLInputElement | null>(null)

const typeLabels: Record<string, string> = {
  PLUGIN: '插件',
  MODULE: '模块',
  SERVICE: '服务',
  TEMPLATE: '模板',
  THEME: '主题',
  TOOL: '工具',
}

const statusLabels: Record<string, string> = {
  DRAFT: '草稿',
  PENDING: '审核中',
  APPROVED: '已发布',
  REJECTED: '已拒绝',
  SUSPENDED: '已下架',
}

const statusClasses: Record<string, string> = {
  DRAFT: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200',
  PENDING: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
  APPROVED: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
  REJECTED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
  SUSPENDED: 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200',
}

onMounted(() => {
  if (authStore.isAuthenticated) {
    marketStore.fetchMyResources()
  }
})

watch(() => authStore.isAuthenticated, (isAuth) => {
  if (isAuth) {
    marketStore.fetchMyResources()
  }
})

const openVersionDialog = (resource: MarketResource) => {
  selectedResource.value = resource
  versionForm.version = ''
  versionForm.changelog = ''
  versionForm.minApiVersion = resource.minApiVersion || '1.0.0'
  versionForm.file = null
  versionError.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  showVersionDialog.value = true
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    versionForm.file = target.files[0]
  }
}

const handlePublishVersion = async () => {
  if (!selectedResource.value || !versionForm.file) {
    versionError.value = '请选择要上传的文件'
    return
  }

  versionError.value = ''

  const result = await marketStore.publishVersion(selectedResource.value.resourceId, {
    version: versionForm.version,
    changelog: versionForm.changelog,
    minApiVersion: versionForm.minApiVersion,
    file: versionForm.file,
  })

  if (result.success) {
    showVersionDialog.value = false
    await marketStore.fetchMyResources()
  } else {
    versionError.value = result.error || '发布失败'
  }
}

const confirmDelete = (resource: MarketResource) => {
  selectedResource.value = resource
  showDeleteDialog.value = true
}

const handleDelete = async () => {
  if (!selectedResource.value) return

  const result = await marketStore.deleteResource(selectedResource.value.resourceId)

  if (result.success) {
    showDeleteDialog.value = false
  }
}

useHead({
  title: '我的资源 - Azathoth',
})
</script>
