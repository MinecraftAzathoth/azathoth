<template>
  <div class="p-6 lg:p-8 max-w-4xl">
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">
        API 文档
      </h1>
      <p class="text-lg text-gray-600 dark:text-gray-400">
        欢迎来到 Azathoth 官网 API 文档。在这里你可以找到所有 API 接口的详细说明。
      </p>
    </div>

    <!-- Quick Links -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
      <NuxtLink
        v-for="item in quickLinks"
        :key="item.to"
        :to="item.to"
        class="block p-6 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-primary-500 dark:hover:border-primary-500 transition-colors"
      >
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">
          {{ item.title }}
        </h3>
        <p class="text-sm text-gray-600 dark:text-gray-400">
          {{ item.description }}
        </p>
      </NuxtLink>
    </div>

    <!-- Getting Started -->
    <div class="prose dark:prose-invert max-w-none">
      <h2>快速开始</h2>
      <p>所有 API 请求都应发送到基础 URL:</p>
      <pre><code>https://api.azathoth.dev/v1</code></pre>

      <h3>认证</h3>
      <p>大多数 API 端点需要认证。在请求头中包含 Bearer Token:</p>
      <pre><code>Authorization: Bearer &lt;access_token&gt;</code></pre>

      <h3>响应格式</h3>
      <p>所有 API 响应均为 JSON 格式：</p>
      <pre><code>{
  "success": true,
  "data": { ... }
}</code></pre>

      <h3>错误处理</h3>
      <p>当请求失败时，API 会返回错误信息：</p>
      <pre><code>{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "错误描述"
  }
}</code></pre>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'wiki'
})

const quickLinks = [
  {
    to: '/wiki/api/auth',
    title: '认证模块',
    description: '用户注册、登录、令牌管理等认证相关接口'
  },
  {
    to: '/wiki/api/market',
    title: '市场模块',
    description: '资源搜索、发布、下载、评论等市场功能接口'
  },
  {
    to: '/wiki/api/payment',
    title: '支付模块',
    description: '订单创建、钱包余额、提现等支付功能接口'
  },
  {
    to: '/wiki/api/generator',
    title: '生成器模块',
    description: '项目脚手架生成、模板管理等接口'
  },
  {
    to: '/wiki/api/forum',
    title: '论坛模块',
    description: '帖子、回复、通知等社区功能接口'
  },
  {
    to: '/wiki/api/review',
    title: '审核模块',
    description: '内容审核、举报处理等管理功能接口'
  },
]

useHead({
  title: 'API 文档 - Azathoth',
  meta: [
    { name: 'description', content: 'Azathoth 官网 API 文档，包含认证、市场、支付、论坛等模块的接口说明' }
  ]
})
</script>

<style scoped>
.prose pre {
  @apply bg-gray-800 text-gray-100 rounded-lg p-4 overflow-x-auto;
}

.prose code {
  @apply text-sm;
}

.prose :not(pre) > code {
  @apply bg-gray-100 dark:bg-gray-800 px-1 py-0.5 rounded text-primary-600 dark:text-primary-400;
}
</style>
