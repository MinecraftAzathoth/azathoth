<template>
  <NuxtLink
    :to="`/market/${resource.slug}`"
    class="block bg-white dark:bg-gray-800 rounded-lg shadow hover:shadow-lg transition-shadow overflow-hidden"
  >
    <div class="aspect-video bg-gray-100 dark:bg-gray-700 relative">
      <img
        :src="resource.icon || '/default-resource-icon.png'"
        :alt="resource.name"
        class="w-full h-full object-cover"
      />
      <div v-if="resource.pricing" class="absolute top-2 right-2 px-2 py-1 bg-primary-600 text-white text-sm rounded">
        ¥{{ (resource.pricing.price / 100).toFixed(0) }}
      </div>
      <div v-else class="absolute top-2 right-2 px-2 py-1 bg-green-600 text-white text-sm rounded">
        {{ $t('common.free') }}
      </div>
    </div>
    <div class="p-4">
      <h3 class="font-semibold text-gray-900 dark:text-white mb-1 line-clamp-1">
        {{ resource.name }}
      </h3>
      <p class="text-sm text-gray-500 dark:text-gray-400 mb-3 line-clamp-2">
        {{ resource.description }}
      </p>
      <div class="flex items-center justify-between text-sm">
        <span class="text-gray-500">{{ resource.authorName }}</span>
        <div class="flex items-center gap-3">
          <span class="flex items-center text-gray-500">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            {{ formatDownloads(resource.downloads) }}
          </span>
          <span class="flex items-center text-yellow-500">
            ★ {{ resource.rating.toFixed(1) }}
          </span>
        </div>
      </div>
    </div>
  </NuxtLink>
</template>

<script setup lang="ts">
import type { MarketResource } from '~/types'

defineProps<{
  resource: MarketResource
}>()

const formatDownloads = (num: number) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}
</script>
