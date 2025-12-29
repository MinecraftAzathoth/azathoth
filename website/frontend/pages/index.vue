<template>
  <div>
    <!-- Hero Section -->
    <section class="bg-gradient-to-br from-primary-600 to-primary-800 text-white py-20">
      <div class="container mx-auto px-4 text-center">
        <h1 class="text-4xl md:text-6xl font-bold mb-6">
          {{ $t('home.hero.title') }}
        </h1>
        <p class="text-xl md:text-2xl text-primary-100 mb-10 max-w-3xl mx-auto">
          {{ $t('home.hero.subtitle') }}
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <NuxtLink
            to="/generator"
            class="px-8 py-4 bg-white text-primary-600 rounded-lg font-semibold hover:bg-primary-50 transition-colors"
          >
            {{ $t('home.hero.getStarted') }}
          </NuxtLink>
          <NuxtLink
            to="/docs"
            class="px-8 py-4 border-2 border-white text-white rounded-lg font-semibold hover:bg-white/10 transition-colors"
          >
            {{ $t('home.hero.viewDocs') }}
          </NuxtLink>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="py-20 bg-white dark:bg-gray-800">
      <div class="container mx-auto px-4">
        <h2 class="text-3xl font-bold text-center mb-12 text-gray-900 dark:text-white">
          {{ $t('home.features.title') }}
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div
            v-for="feature in features"
            :key="feature.key"
            class="p-6 bg-gray-50 dark:bg-gray-700 rounded-xl"
          >
            <div class="w-12 h-12 bg-primary-100 dark:bg-primary-900 rounded-lg flex items-center justify-center mb-4">
              <component :is="feature.icon" class="w-6 h-6 text-primary-600 dark:text-primary-400" />
            </div>
            <h3 class="text-xl font-semibold mb-2 text-gray-900 dark:text-white">
              {{ $t(`home.features.${feature.key}.title`) }}
            </h3>
            <p class="text-gray-600 dark:text-gray-300">
              {{ $t(`home.features.${feature.key}.description`) }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Popular Resources Section -->
    <section class="py-20 bg-gray-50 dark:bg-gray-900">
      <div class="container mx-auto px-4">
        <div class="flex justify-between items-center mb-8">
          <h2 class="text-3xl font-bold text-gray-900 dark:text-white">
            {{ $t('home.popular.title') }}
          </h2>
          <NuxtLink
            to="/market?sort=downloads"
            class="text-primary-600 dark:text-primary-400 hover:underline"
          >
            {{ $t('home.popular.viewAll') }} &rarr;
          </NuxtLink>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <ResourceCard
            v-for="resource in popularResources"
            :key="resource.resourceId"
            :resource="resource"
          />
        </div>
      </div>
    </section>

    <!-- Latest Resources Section -->
    <section class="py-20 bg-white dark:bg-gray-800">
      <div class="container mx-auto px-4">
        <div class="flex justify-between items-center mb-8">
          <h2 class="text-3xl font-bold text-gray-900 dark:text-white">
            {{ $t('home.latest.title') }}
          </h2>
          <NuxtLink
            to="/market?sort=created"
            class="text-primary-600 dark:text-primary-400 hover:underline"
          >
            {{ $t('home.latest.viewAll') }} &rarr;
          </NuxtLink>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <ResourceCard
            v-for="resource in latestResources"
            :key="resource.resourceId"
            :resource="resource"
          />
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-20 bg-primary-600">
      <div class="container mx-auto px-4 text-center">
        <h2 class="text-3xl font-bold text-white mb-6">
          准备好开始了吗？
        </h2>
        <p class="text-xl text-primary-100 mb-8 max-w-2xl mx-auto">
          立即使用项目生成器创建你的第一个 Azathoth 插件
        </p>
        <NuxtLink
          to="/generator"
          class="inline-block px-8 py-4 bg-white text-primary-600 rounded-lg font-semibold hover:bg-primary-50 transition-colors"
        >
          {{ $t('home.hero.getStarted') }}
        </NuxtLink>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import type { MarketResource } from '~/types'

const { t } = useI18n()
const marketStore = useMarketStore()

// Feature icons (simplified as text for now)
const features = [
  { key: 'performance', icon: 'div' },
  { key: 'modular', icon: 'div' },
  { key: 'developer', icon: 'div' },
  { key: 'community', icon: 'div' },
]

const popularResources = ref<MarketResource[]>([])
const latestResources = ref<MarketResource[]>([])

onMounted(async () => {
  popularResources.value = await marketStore.fetchPopular(undefined, 4)
  latestResources.value = await marketStore.fetchLatest(undefined, 4)
})

// SEO
useHead({
  title: 'Azathoth - Minecraft MMORPG Framework',
  meta: [
    { name: 'description', content: t('home.hero.subtitle') }
  ]
})
</script>
