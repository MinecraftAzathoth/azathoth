<template>
  <div class="container mx-auto px-4 py-8">
    <!-- Loading -->
    <div v-if="loading" class="text-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
    </div>

    <!-- Resource Details -->
    <div v-else-if="resource">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Main Content -->
        <div class="lg:col-span-2">
          <!-- Header -->
          <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
            <div class="flex items-start gap-4">
              <img
                :src="resource.icon || '/default-resource-icon.png'"
                :alt="resource.name"
                class="w-20 h-20 rounded-lg object-cover"
              />
              <div class="flex-1">
                <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-2">
                  {{ resource.name }}
                </h1>
                <div class="flex flex-wrap gap-2 mb-3">
                  <span class="px-2 py-1 bg-primary-100 dark:bg-primary-900 text-primary-700 dark:text-primary-300 text-sm rounded">
                    {{ $t(`market.types.${resource.type}`) }}
                  </span>
                  <span class="px-2 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 text-sm rounded">
                    {{ $t(`market.licenses.${resource.license}`) }}
                  </span>
                </div>
                <p class="text-gray-600 dark:text-gray-400">
                  by <NuxtLink :to="`/user/${resource.authorId}`" class="text-primary-600 hover:underline">{{ resource.authorName }}</NuxtLink>
                </p>
              </div>
            </div>
          </div>

          <!-- Tabs -->
          <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
            <div class="border-b border-gray-200 dark:border-gray-700">
              <nav class="flex -mb-px">
                <button
                  v-for="tab in tabs"
                  :key="tab.id"
                  @click="activeTab = tab.id"
                  :class="[
                    'px-6 py-4 text-sm font-medium border-b-2 transition-colors',
                    activeTab === tab.id
                      ? 'border-primary-600 text-primary-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  ]"
                >
                  {{ tab.label }}
                </button>
              </nav>
            </div>

            <div class="p-6">
              <!-- Description Tab -->
              <div v-if="activeTab === 'description'" class="prose dark:prose-invert max-w-none">
                <div v-html="renderedDescription"></div>
              </div>

              <!-- Changelog Tab -->
              <div v-else-if="activeTab === 'changelog'">
                <div v-for="version in resource.versions" :key="version.version" class="mb-6 last:mb-0">
                  <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                    v{{ version.version }}
                    <span class="text-sm font-normal text-gray-500">
                      - {{ formatDate(version.releasedAt) }}
                    </span>
                  </h3>
                  <div class="text-gray-600 dark:text-gray-400 whitespace-pre-wrap">
                    {{ version.changelog }}
                  </div>
                </div>
              </div>

              <!-- Reviews Tab -->
              <div v-else-if="activeTab === 'reviews'">
                <div v-if="reviews.length">
                  <div v-for="review in reviews" :key="review.reviewId" class="mb-6 pb-6 border-b border-gray-200 dark:border-gray-700 last:border-0">
                    <div class="flex items-center gap-3 mb-2">
                      <span class="font-semibold text-gray-900 dark:text-white">{{ review.userName }}</span>
                      <div class="flex text-yellow-400">
                        <span v-for="i in 5" :key="i" :class="i <= review.rating ? 'text-yellow-400' : 'text-gray-300'">
                          ★
                        </span>
                      </div>
                      <span class="text-sm text-gray-500">{{ formatDate(review.createdAt) }}</span>
                    </div>
                    <p class="text-gray-600 dark:text-gray-400">{{ review.content }}</p>
                    <div v-if="review.authorReply" class="mt-3 pl-4 border-l-2 border-primary-500">
                      <p class="text-sm text-gray-500 mb-1">作者回复：</p>
                      <p class="text-gray-600 dark:text-gray-400">{{ review.authorReply }}</p>
                    </div>
                  </div>
                </div>
                <p v-else class="text-gray-500 dark:text-gray-400">暂无评价</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="lg:col-span-1">
          <!-- Download/Purchase Card -->
          <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
            <div v-if="resource.pricing" class="text-center mb-4">
              <span class="text-3xl font-bold text-gray-900 dark:text-white">
                ¥{{ (resource.pricing.price / 100).toFixed(2) }}
              </span>
              <span v-if="resource.pricing.subscriptionPeriod" class="text-gray-500">
                /{{ resource.pricing.subscriptionPeriod }}天
              </span>
            </div>
            <div v-else class="text-center mb-4">
              <span class="text-2xl font-bold text-green-600">{{ $t('common.free') }}</span>
            </div>

            <button
              v-if="resource.pricing"
              @click="handlePurchase"
              class="w-full py-3 bg-primary-600 text-white rounded-lg font-semibold hover:bg-primary-700 transition-colors mb-3"
            >
              {{ $t('market.resource.purchase') }}
            </button>
            <button
              v-else
              @click="handleDownload"
              class="w-full py-3 bg-green-600 text-white rounded-lg font-semibold hover:bg-green-700 transition-colors mb-3"
            >
              {{ $t('market.resource.download') }}
            </button>

            <!-- Stats -->
            <div class="grid grid-cols-2 gap-4 pt-4 border-t border-gray-200 dark:border-gray-700">
              <div class="text-center">
                <div class="text-2xl font-bold text-gray-900 dark:text-white">
                  {{ formatNumber(resource.downloads) }}
                </div>
                <div class="text-sm text-gray-500">{{ $t('market.resource.downloads') }}</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-gray-900 dark:text-white flex items-center justify-center">
                  {{ resource.rating.toFixed(1) }}
                  <span class="text-yellow-400 ml-1">★</span>
                </div>
                <div class="text-sm text-gray-500">{{ $t('market.resource.rating') }}</div>
              </div>
            </div>
          </div>

          <!-- Info Card -->
          <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
            <h3 class="font-semibold text-gray-900 dark:text-white mb-4">资源信息</h3>
            <dl class="space-y-3 text-sm">
              <div class="flex justify-between">
                <dt class="text-gray-500">{{ $t('market.resource.version') }}</dt>
                <dd class="text-gray-900 dark:text-white">{{ resource.latestVersion }}</dd>
              </div>
              <div class="flex justify-between">
                <dt class="text-gray-500">最低 API</dt>
                <dd class="text-gray-900 dark:text-white">{{ resource.minApiVersion }}</dd>
              </div>
              <div class="flex justify-between">
                <dt class="text-gray-500">{{ $t('market.resource.updated') }}</dt>
                <dd class="text-gray-900 dark:text-white">{{ formatDate(resource.updatedAt) }}</dd>
              </div>
            </dl>
          </div>

          <!-- Tags -->
          <div v-if="resource.tags.length" class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <h3 class="font-semibold text-gray-900 dark:text-white mb-4">标签</h3>
            <div class="flex flex-wrap gap-2">
              <NuxtLink
                v-for="tag in resource.tags"
                :key="tag"
                :to="`/market?tags=${tag}`"
                class="px-3 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-full text-sm hover:bg-gray-200 dark:hover:bg-gray-600"
              >
                {{ tag }}
              </NuxtLink>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Not Found -->
    <div v-else class="text-center py-20">
      <p class="text-gray-500 dark:text-gray-400">资源不存在</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { MarketResource, ResourceReview } from '~/types'

const { t } = useI18n()
const route = useRoute()
const marketStore = useMarketStore()

const loading = ref(true)
const resource = computed(() => marketStore.currentResource)
const reviews = ref<ResourceReview[]>([])
const activeTab = ref('description')

const tabs = computed(() => [
  { id: 'description', label: '介绍' },
  { id: 'changelog', label: t('market.resource.changelog') },
  { id: 'reviews', label: `${t('market.resource.reviews')} (${resource.value?.reviewCount || 0})` },
])

const renderedDescription = computed(() => {
  // In real app, use markdown renderer
  return resource.value?.description.replace(/\n/g, '<br>') || ''
})

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString()
}

const formatNumber = (num: number) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

const handleDownload = async () => {
  // TODO: Implement download
  alert('下载功能开发中')
}

const handlePurchase = async () => {
  // TODO: Implement purchase
  alert('购买功能开发中')
}

onMounted(async () => {
  const slug = route.params.slug as string
  await marketStore.fetchResourceBySlug(slug)
  loading.value = false
})

useHead(() => ({
  title: resource.value ? `${resource.value.name} - Azathoth Market` : 'Loading...',
}))
</script>
