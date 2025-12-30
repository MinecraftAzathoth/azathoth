<template>
  <div class="container mx-auto px-4 py-8 max-w-4xl">
    <h1 class="text-3xl font-bold mb-8 text-gray-900 dark:text-white">
      发布资源
    </h1>

    <!-- 未登录提示 -->
    <div
      v-if="!authStore.isAuthenticated"
      class="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-6 text-center"
    >
      <p class="text-yellow-800 dark:text-yellow-200 mb-4">请先登录后再发布资源</p>
      <NuxtLink
        to="/auth/login"
        class="inline-block px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
      >
        前往登录
      </NuxtLink>
    </div>

    <!-- 发布表单 -->
    <form v-else @submit.prevent="handleSubmit" class="space-y-6">
      <!-- 基本信息 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold mb-4 text-gray-900 dark:text-white">基本信息</h2>

        <div class="space-y-4">
          <!-- 资源名称 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              资源名称 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.name"
              type="text"
              required
              maxlength="100"
              placeholder="输入资源名称"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500"
            />
          </div>

          <!-- 资源描述 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              资源描述 <span class="text-red-500">*</span>
            </label>
            <textarea
              v-model="form.description"
              required
              rows="5"
              maxlength="5000"
              placeholder="详细描述您的资源功能、特性等"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500"
            />
            <p class="text-sm text-gray-500 mt-1">{{ form.description.length }} / 5000</p>
          </div>

          <!-- 资源类型 -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                资源类型 <span class="text-red-500">*</span>
              </label>
              <select
                v-model="form.type"
                required
                class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="" disabled>选择类型</option>
                <option v-for="type in resourceTypes" :key="type" :value="type">
                  {{ typeLabels[type] }}
                </option>
              </select>
            </div>

            <!-- 授权类型 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                授权类型 <span class="text-red-500">*</span>
              </label>
              <select
                v-model="form.license"
                required
                class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option value="" disabled>选择授权</option>
                <option v-for="license in licenseTypes" :key="license" :value="license">
                  {{ licenseLabels[license] }}
                </option>
              </select>
            </div>
          </div>

          <!-- 最低 API 版本 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              最低 API 版本 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.minApiVersion"
              type="text"
              required
              placeholder="例如: 1.0.0"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500"
            />
          </div>
        </div>
      </div>

      <!-- 定价信息 -->
      <div v-if="isPaidLicense" class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold mb-4 text-gray-900 dark:text-white">定价信息</h2>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              价格 <span class="text-red-500">*</span>
            </label>
            <input
              v-model.number="form.pricing.price"
              type="number"
              min="0"
              step="1"
              required
              placeholder="以分为单位"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            />
            <p class="text-sm text-gray-500 mt-1">以分为单位，100 = 1元</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              货币
            </label>
            <select
              v-model="form.pricing.currency"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            >
              <option value="CNY">CNY (人民币)</option>
              <option value="USD">USD (美元)</option>
            </select>
          </div>

          <div v-if="form.license === 'PAID_SUBSCRIPTION'">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              订阅周期 (天)
            </label>
            <input
              v-model.number="form.pricing.subscriptionPeriod"
              type="number"
              min="1"
              placeholder="30"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            />
          </div>
        </div>
      </div>

      <!-- 标签 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold mb-4 text-gray-900 dark:text-white">标签</h2>

        <div class="flex flex-wrap gap-2 mb-4">
          <span
            v-for="tag in form.tags"
            :key="tag"
            class="inline-flex items-center px-3 py-1 bg-primary-100 dark:bg-primary-900 text-primary-800 dark:text-primary-200 rounded-full text-sm"
          >
            {{ tag }}
            <button
              type="button"
              @click="removeTag(tag)"
              class="ml-2 text-primary-600 dark:text-primary-400 hover:text-primary-800"
            >
              &times;
            </button>
          </span>
        </div>

        <div class="flex gap-2">
          <input
            v-model="newTag"
            type="text"
            placeholder="添加标签"
            maxlength="20"
            @keyup.enter.prevent="addTag"
            class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          />
          <button
            type="button"
            @click="addTag"
            class="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-gray-200 rounded-lg hover:bg-gray-300 dark:hover:bg-gray-500"
          >
            添加
          </button>
        </div>
        <p class="text-sm text-gray-500 mt-2">最多添加 10 个标签</p>
      </div>

      <!-- 依赖 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold mb-4 text-gray-900 dark:text-white">依赖项</h2>

        <div class="flex flex-wrap gap-2 mb-4">
          <span
            v-for="dep in form.dependencies"
            :key="dep"
            class="inline-flex items-center px-3 py-1 bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-full text-sm"
          >
            {{ dep }}
            <button
              type="button"
              @click="removeDependency(dep)"
              class="ml-2 text-gray-600 dark:text-gray-400 hover:text-gray-800"
            >
              &times;
            </button>
          </span>
        </div>

        <div class="flex gap-2">
          <input
            v-model="newDependency"
            type="text"
            placeholder="添加依赖资源 ID"
            @keyup.enter.prevent="addDependency"
            class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
          />
          <button
            type="button"
            @click="addDependency"
            class="px-4 py-2 bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-gray-200 rounded-lg hover:bg-gray-300 dark:hover:bg-gray-500"
          >
            添加
          </button>
        </div>
      </div>

      <!-- 错误提示 -->
      <div
        v-if="error"
        class="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4"
      >
        <p class="text-red-800 dark:text-red-200">{{ error }}</p>
      </div>

      <!-- 提交按钮 -->
      <div class="flex justify-end gap-4">
        <NuxtLink
          to="/market"
          class="px-6 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
        >
          取消
        </NuxtLink>
        <button
          type="submit"
          :disabled="marketStore.submitting"
          class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ marketStore.submitting ? '提交中...' : '创建资源' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ResourceType, LicenseType } from '~/types'

const router = useRouter()
const authStore = useAuthStore()
const marketStore = useMarketStore()

const resourceTypes = Object.values(ResourceType)
const licenseTypes = Object.values(LicenseType)

const typeLabels: Record<string, string> = {
  PLUGIN: '插件',
  MODULE: '模块',
  SERVICE: '服务',
  TEMPLATE: '模板',
  THEME: '主题',
  TOOL: '工具',
}

const licenseLabels: Record<string, string> = {
  FREE_OPEN_SOURCE: '免费开源',
  FREE_CLOSED_SOURCE: '免费闭源',
  PAID_PERPETUAL: '付费永久',
  PAID_SUBSCRIPTION: '付费订阅',
}

const form = reactive({
  name: '',
  description: '',
  type: '',
  license: '',
  minApiVersion: '1.0.0',
  pricing: {
    price: 0,
    currency: 'CNY',
    subscriptionPeriod: 30,
  },
  tags: [] as string[],
  dependencies: [] as string[],
})

const newTag = ref('')
const newDependency = ref('')
const error = ref('')

const isPaidLicense = computed(() => {
  return form.license === 'PAID_PERPETUAL' || form.license === 'PAID_SUBSCRIPTION'
})

const addTag = () => {
  const tag = newTag.value.trim()
  if (tag && !form.tags.includes(tag) && form.tags.length < 10) {
    form.tags.push(tag)
    newTag.value = ''
  }
}

const removeTag = (tag: string) => {
  form.tags = form.tags.filter((t) => t !== tag)
}

const addDependency = () => {
  const dep = newDependency.value.trim()
  if (dep && !form.dependencies.includes(dep)) {
    form.dependencies.push(dep)
    newDependency.value = ''
  }
}

const removeDependency = (dep: string) => {
  form.dependencies = form.dependencies.filter((d) => d !== dep)
}

const handleSubmit = async () => {
  error.value = ''

  if (!form.name.trim()) {
    error.value = '请输入资源名称'
    return
  }

  if (!form.description.trim()) {
    error.value = '请输入资源描述'
    return
  }

  if (!form.type) {
    error.value = '请选择资源类型'
    return
  }

  if (!form.license) {
    error.value = '请选择授权类型'
    return
  }

  const data: any = {
    name: form.name.trim(),
    description: form.description.trim(),
    type: form.type,
    license: form.license,
    minApiVersion: form.minApiVersion,
    tags: form.tags,
    dependencies: form.dependencies,
  }

  if (isPaidLicense.value) {
    data.pricing = {
      price: form.pricing.price,
      currency: form.pricing.currency,
      subscriptionPeriod: form.license === 'PAID_SUBSCRIPTION' ? form.pricing.subscriptionPeriod : undefined,
    }
  }

  const result = await marketStore.createResource(data)

  if (result.success && result.resource) {
    router.push(`/market/${result.resource.slug}`)
  } else {
    error.value = result.error || '创建失败'
  }
}

useHead({
  title: '发布资源 - Azathoth',
})
</script>
