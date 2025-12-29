<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-8 text-gray-900 dark:text-white">
      {{ $t('market.title') }}
    </h1>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 mb-8">
      <div class="flex flex-wrap gap-4">
        <!-- Search -->
        <div class="flex-1 min-w-[200px]">
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="$t('market.search')"
            class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            @keyup.enter="applyFilters"
          />
        </div>

        <!-- Type Filter -->
        <select
          v-model="selectedType"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
        >
          <option :value="null">{{ $t('market.filters.type') }}</option>
          <option v-for="type in resourceTypes" :key="type" :value="type">
            {{ $t(`market.types.${type}`) }}
          </option>
        </select>

        <!-- License Filter -->
        <select
          v-model="selectedLicense"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
        >
          <option :value="null">{{ $t('market.filters.license') }}</option>
          <option v-for="license in licenseTypes" :key="license" :value="license">
            {{ $t(`market.licenses.${license}`) }}
          </option>
        </select>

        <!-- Sort By -->
        <select
          v-model="sortBy"
          class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
        >
          <option v-for="option in sortOptions" :key="option" :value="option">
            {{ $t(`market.sortOptions.${option}`) }}
          </option>
        </select>

        <button
          @click="applyFilters"
          class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
        >
          搜索
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="marketStore.loading" class="text-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
      <p class="mt-4 text-gray-500 dark:text-gray-400">{{ $t('common.loading') }}</p>
    </div>

    <!-- Resources Grid -->
    <div v-else-if="marketStore.resources.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <ResourceCard
        v-for="resource in marketStore.resources"
        :key="resource.resourceId"
        :resource="resource"
      />
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-20">
      <p class="text-gray-500 dark:text-gray-400">{{ $t('search.noResults') }}</p>
    </div>

    <!-- Pagination -->
    <div v-if="marketStore.pagination && marketStore.pagination.totalPages > 1" class="mt-8 flex justify-center">
      <nav class="flex items-center space-x-2">
        <button
          :disabled="marketStore.pagination.page === 1"
          @click="goToPage(marketStore.pagination!.page - 1)"
          class="px-3 py-2 rounded-lg bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 disabled:opacity-50"
        >
          {{ $t('common.previous') }}
        </button>

        <span class="px-4 py-2 text-gray-600 dark:text-gray-300">
          {{ marketStore.pagination.page }} / {{ marketStore.pagination.totalPages }}
        </span>

        <button
          :disabled="marketStore.pagination.page === marketStore.pagination.totalPages"
          @click="goToPage(marketStore.pagination!.page + 1)"
          class="px-3 py-2 rounded-lg bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 disabled:opacity-50"
        >
          {{ $t('common.next') }}
        </button>
      </nav>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ResourceType, LicenseType } from '~/types'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const marketStore = useMarketStore()

const searchQuery = ref('')
const selectedType = ref<ResourceType | null>(null)
const selectedLicense = ref<LicenseType | null>(null)
const sortBy = ref('DOWNLOADS')

const resourceTypes = Object.values(ResourceType)
const licenseTypes = Object.values(LicenseType)
const sortOptions = ['DOWNLOADS', 'RATING', 'UPDATED', 'CREATED']

// Initialize from URL params
onMounted(() => {
  const query = route.query
  if (query.keyword) searchQuery.value = query.keyword as string
  if (query.type) selectedType.value = query.type as ResourceType
  if (query.license) selectedLicense.value = query.license as LicenseType
  if (query.sort) sortBy.value = query.sort as string

  applyFilters()
})

const applyFilters = () => {
  marketStore.setFilters({
    keyword: searchQuery.value,
    type: selectedType.value,
    license: selectedLicense.value,
    sortBy: sortBy.value,
  })
  marketStore.fetchResources(1)

  // Update URL
  const query: Record<string, string> = {}
  if (searchQuery.value) query.keyword = searchQuery.value
  if (selectedType.value) query.type = selectedType.value
  if (selectedLicense.value) query.license = selectedLicense.value
  if (sortBy.value !== 'DOWNLOADS') query.sort = sortBy.value

  router.push({ query })
}

const goToPage = (page: number) => {
  marketStore.fetchResources(page)
}

useHead({
  title: t('market.title') + ' - Azathoth',
})
</script>
