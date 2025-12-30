<template>
  <div>
    <h1 class="text-4xl font-bold text-gray-900 dark:text-white mb-4">
      Azathoth 开发者文档
    </h1>
    <p class="text-xl text-gray-600 dark:text-gray-400 mb-8">
      欢迎使用 Azathoth MMORPG 开发框架。本文档将帮助您快速上手并构建高质量的 Minecraft MMO 服务器。
    </p>

    <!-- Quick Links -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-12">
      <NuxtLink
        to="/docs/getting-started"
        class="block p-6 bg-white dark:bg-gray-800 rounded-lg shadow hover:shadow-lg transition-shadow border border-gray-200 dark:border-gray-700"
      >
        <div class="flex items-center mb-3">
          <div class="w-10 h-10 bg-primary-100 dark:bg-primary-900 rounded-lg flex items-center justify-center mr-3">
            <svg class="w-6 h-6 text-primary-600 dark:text-primary-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">快速开始</h2>
        </div>
        <p class="text-gray-600 dark:text-gray-400 text-sm">
          在几分钟内创建您的第一个 Azathoth 项目，了解基本概念和工作流程。
        </p>
      </NuxtLink>

      <NuxtLink
        to="/docs/core/architecture"
        class="block p-6 bg-white dark:bg-gray-800 rounded-lg shadow hover:shadow-lg transition-shadow border border-gray-200 dark:border-gray-700"
      >
        <div class="flex items-center mb-3">
          <div class="w-10 h-10 bg-green-100 dark:bg-green-900 rounded-lg flex items-center justify-center mr-3">
            <svg class="w-6 h-6 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
          </div>
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">架构设计</h2>
        </div>
        <p class="text-gray-600 dark:text-gray-400 text-sm">
          深入了解 Azathoth 的模块化架构、依赖注入系统和插件生命周期。
        </p>
      </NuxtLink>

      <NuxtLink
        to="/docs/systems/skill"
        class="block p-6 bg-white dark:bg-gray-800 rounded-lg shadow hover:shadow-lg transition-shadow border border-gray-200 dark:border-gray-700"
      >
        <div class="flex items-center mb-3">
          <div class="w-10 h-10 bg-purple-100 dark:bg-purple-900 rounded-lg flex items-center justify-center mr-3">
            <svg class="w-6 h-6 text-purple-600 dark:text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
            </svg>
          </div>
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">游戏系统</h2>
        </div>
        <p class="text-gray-600 dark:text-gray-400 text-sm">
          探索技能、副本、任务、物品等核心游戏系统的实现和扩展方式。
        </p>
      </NuxtLink>

      <NuxtLink
        to="/docs/api/overview"
        class="block p-6 bg-white dark:bg-gray-800 rounded-lg shadow hover:shadow-lg transition-shadow border border-gray-200 dark:border-gray-700"
      >
        <div class="flex items-center mb-3">
          <div class="w-10 h-10 bg-orange-100 dark:bg-orange-900 rounded-lg flex items-center justify-center mr-3">
            <svg class="w-6 h-6 text-orange-600 dark:text-orange-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
            </svg>
          </div>
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">API 参考</h2>
        </div>
        <p class="text-gray-600 dark:text-gray-400 text-sm">
          完整的 API 文档，包含所有公开类、接口、注解和方法的详细说明。
        </p>
      </NuxtLink>
    </div>

    <!-- Features -->
    <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">核心特性</h2>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
      <div class="p-4">
        <h3 class="font-semibold text-gray-900 dark:text-white mb-2">模块化架构</h3>
        <p class="text-gray-600 dark:text-gray-400 text-sm">
          基于 Koin 的依赖注入，支持热插拔模块，便于扩展和维护。
        </p>
      </div>
      <div class="p-4">
        <h3 class="font-semibold text-gray-900 dark:text-white mb-2">高性能网络</h3>
        <p class="text-gray-600 dark:text-gray-400 text-sm">
          基于 Netty 的高性能网络层，支持 gRPC 和 WebSocket 通信。
        </p>
      </div>
      <div class="p-4">
        <h3 class="font-semibold text-gray-900 dark:text-white mb-2">丰富的游戏系统</h3>
        <p class="text-gray-600 dark:text-gray-400 text-sm">
          内置技能、副本、任务、物品等 MMO 核心系统，开箱即用。
        </p>
      </div>
      <div class="p-4">
        <h3 class="font-semibold text-gray-900 dark:text-white mb-2">智能 AI 系统</h3>
        <p class="text-gray-600 dark:text-gray-400 text-sm">
          基于行为树的 AI 系统，支持复杂的 NPC 行为定义。
        </p>
      </div>
      <div class="p-4">
        <h3 class="font-semibold text-gray-900 dark:text-white mb-2">数据持久化</h3>
        <p class="text-gray-600 dark:text-gray-400 text-sm">
          支持 PostgreSQL、MySQL、MongoDB，集成 Redis 缓存。
        </p>
      </div>
      <div class="p-4">
        <h3 class="font-semibold text-gray-900 dark:text-white mb-2">开发工具链</h3>
        <p class="text-gray-600 dark:text-gray-400 text-sm">
          Gradle 插件、CLI 工具、项目生成器，提升开发效率。
        </p>
      </div>
    </div>

    <!-- Code Example -->
    <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">示例代码</h2>
    <div class="bg-gray-900 rounded-lg p-6 mb-8 overflow-x-auto">
      <pre class="text-sm text-gray-300"><code>@AzathothPlugin(
    id = "my-plugin",
    name = "My First Plugin",
    version = "1.0.0"
)
class MyPlugin : AzathothPluginBase() {

    @Inject
    lateinit var skillService: SkillService

    override fun onEnable() {
        logger.info { "Plugin enabled!" }

        // 注册自定义技能
        skillService.registerSkill(FireballSkill())
    }

    override fun onDisable() {
        logger.info { "Plugin disabled!" }
    }
}</code></pre>
    </div>

    <!-- Next Steps -->
    <div class="bg-primary-50 dark:bg-primary-900/20 border border-primary-200 dark:border-primary-800 rounded-lg p-6">
      <h3 class="font-semibold text-primary-900 dark:text-primary-100 mb-2">下一步</h3>
      <p class="text-primary-800 dark:text-primary-200 mb-4">
        准备好开始了吗？按照快速开始指南创建您的第一个项目。
      </p>
      <NuxtLink
        to="/docs/getting-started"
        class="inline-flex items-center text-primary-600 dark:text-primary-400 font-medium hover:underline"
      >
        开始使用
        <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </NuxtLink>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'docs',
})

useHead({
  title: '开发者文档 - Azathoth',
})
</script>
