<template>
  <div class="p-6 lg:p-8 max-w-4xl">
    <ContentDoc v-slot="{ doc }">
      <article class="prose dark:prose-invert max-w-none prose-headings:scroll-mt-20">
        <!-- Breadcrumb -->
        <nav class="text-sm mb-6 not-prose">
          <ol class="flex items-center space-x-2 text-gray-500 dark:text-gray-400">
            <li>
              <NuxtLink to="/wiki" class="hover:text-primary-600 dark:hover:text-primary-400">
                文档
              </NuxtLink>
            </li>
            <li>/</li>
            <li class="text-gray-900 dark:text-white">{{ doc.title }}</li>
          </ol>
        </nav>

        <!-- Title -->
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
          {{ doc.title }}
        </h1>
        <p v-if="doc.description" class="text-lg text-gray-600 dark:text-gray-400 mb-8">
          {{ doc.description }}
        </p>

        <!-- Content -->
        <ContentRenderer :value="doc" />

        <!-- Navigation -->
        <div class="mt-12 pt-8 border-t border-gray-200 dark:border-gray-700 not-prose">
          <div class="flex justify-between">
            <NuxtLink
              v-if="prevDoc"
              :to="prevDoc.to"
              class="flex items-center space-x-2 text-primary-600 dark:text-primary-400 hover:underline"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
              </svg>
              <span>{{ prevDoc.label }}</span>
            </NuxtLink>
            <div v-else></div>
            <NuxtLink
              v-if="nextDoc"
              :to="nextDoc.to"
              class="flex items-center space-x-2 text-primary-600 dark:text-primary-400 hover:underline"
            >
              <span>{{ nextDoc.label }}</span>
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </NuxtLink>
          </div>
        </div>
      </article>
    </ContentDoc>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'wiki'
})

const route = useRoute()

const sidebarItems = [
  { to: '/wiki/api', label: '概述' },
  { to: '/wiki/api/auth', label: '认证模块' },
  { to: '/wiki/api/market', label: '市场模块' },
  { to: '/wiki/api/payment', label: '支付模块' },
  { to: '/wiki/api/generator', label: '生成器模块' },
  { to: '/wiki/api/forum', label: '论坛模块' },
  { to: '/wiki/api/review', label: '审核模块' },
]

const currentIndex = computed(() => {
  const path = route.path.replace(/\/$/, '')
  return sidebarItems.findIndex(item => item.to === path)
})

const prevDoc = computed(() => {
  if (currentIndex.value > 0) {
    return sidebarItems[currentIndex.value - 1]
  }
  return null
})

const nextDoc = computed(() => {
  if (currentIndex.value >= 0 && currentIndex.value < sidebarItems.length - 1) {
    return sidebarItems[currentIndex.value + 1]
  }
  return null
})

// SEO
const { data: doc } = await useAsyncData(`content-${route.path}`, () => {
  return queryContent(route.path).findOne()
})

useHead({
  title: doc.value?.title ? `${doc.value.title} - Azathoth` : 'API 文档 - Azathoth',
  meta: [
    { name: 'description', content: doc.value?.description || 'Azathoth API 文档' }
  ]
})
</script>

<style>
/* Prose styling for markdown content */
.prose h1 {
  @apply text-2xl font-bold text-gray-900 dark:text-white mt-8 mb-4;
}

.prose h2 {
  @apply text-xl font-bold text-gray-900 dark:text-white mt-8 mb-4 pb-2 border-b border-gray-200 dark:border-gray-700;
}

.prose h3 {
  @apply text-lg font-semibold text-gray-900 dark:text-white mt-6 mb-3;
}

.prose h4 {
  @apply text-base font-semibold text-gray-900 dark:text-white mt-4 mb-2;
}

.prose p {
  @apply text-gray-700 dark:text-gray-300 my-4 leading-relaxed;
}

.prose ul {
  @apply list-disc list-inside my-4 space-y-1 text-gray-700 dark:text-gray-300;
}

.prose ol {
  @apply list-decimal list-inside my-4 space-y-1 text-gray-700 dark:text-gray-300;
}

.prose li {
  @apply text-gray-700 dark:text-gray-300;
}

.prose a {
  @apply text-primary-600 dark:text-primary-400 hover:underline;
}

.prose code {
  @apply text-sm font-mono;
}

.prose :not(pre) > code {
  @apply bg-gray-100 dark:bg-gray-800 px-1.5 py-0.5 rounded text-primary-600 dark:text-primary-400;
}

.prose pre {
  @apply bg-gray-900 text-gray-100 rounded-lg p-4 overflow-x-auto my-4;
}

.prose pre code {
  @apply bg-transparent p-0 text-gray-100;
}

.prose table {
  @apply w-full my-4 border-collapse;
}

.prose th {
  @apply bg-gray-100 dark:bg-gray-800 px-4 py-2 text-left text-sm font-semibold text-gray-900 dark:text-white border border-gray-200 dark:border-gray-700;
}

.prose td {
  @apply px-4 py-2 text-sm text-gray-700 dark:text-gray-300 border border-gray-200 dark:border-gray-700;
}

.prose hr {
  @apply my-8 border-gray-200 dark:border-gray-700;
}

.prose blockquote {
  @apply border-l-4 border-primary-500 pl-4 italic text-gray-600 dark:text-gray-400 my-4;
}

.prose strong {
  @apply font-semibold text-gray-900 dark:text-white;
}

.prose img {
  @apply rounded-lg my-4;
}
</style>
