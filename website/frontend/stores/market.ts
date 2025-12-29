import { defineStore } from 'pinia'
import type { MarketResource, ResourceType, LicenseType, Pagination } from '~/types'

interface MarketState {
  resources: MarketResource[]
  currentResource: MarketResource | null
  pagination: Pagination | null
  loading: boolean
  filters: {
    keyword: string
    type: ResourceType | null
    license: LicenseType | null
    tags: string[]
    sortBy: string
    sortOrder: 'ASC' | 'DESC'
  }
}

export const useMarketStore = defineStore('market', {
  state: (): MarketState => ({
    resources: [],
    currentResource: null,
    pagination: null,
    loading: false,
    filters: {
      keyword: '',
      type: null,
      license: null,
      tags: [],
      sortBy: 'DOWNLOADS',
      sortOrder: 'DESC',
    },
  }),

  actions: {
    async fetchResources(page: number = 1, pageSize: number = 20) {
      this.loading = true
      try {
        const params = new URLSearchParams()
        params.set('page', page.toString())
        params.set('pageSize', pageSize.toString())

        if (this.filters.keyword) params.set('keyword', this.filters.keyword)
        if (this.filters.type) params.set('type', this.filters.type)
        if (this.filters.license) params.set('license', this.filters.license)
        if (this.filters.tags.length) params.set('tags', this.filters.tags.join(','))
        params.set('sortBy', this.filters.sortBy)
        params.set('sortOrder', this.filters.sortOrder)

        const result = await $fetch<{
          success: boolean
          resources: MarketResource[]
          pagination: Pagination
        }>(`/api/market/resources?${params}`)

        if (result.success) {
          this.resources = result.resources
          this.pagination = result.pagination
        }
      } catch (error) {
        console.error('Failed to fetch resources:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchResource(resourceId: string) {
      this.loading = true
      try {
        const result = await $fetch<{
          success: boolean
          resource: MarketResource
        }>(`/api/market/resources/${resourceId}`)

        if (result.success) {
          this.currentResource = result.resource
        }
      } catch (error) {
        console.error('Failed to fetch resource:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchResourceBySlug(slug: string) {
      this.loading = true
      try {
        const result = await $fetch<{
          success: boolean
          resource: MarketResource
        }>(`/api/market/resources/slug/${slug}`)

        if (result.success) {
          this.currentResource = result.resource
        }
      } catch (error) {
        console.error('Failed to fetch resource:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchPopular(type?: ResourceType, limit: number = 10) {
      try {
        const params = new URLSearchParams()
        if (type) params.set('type', type)
        params.set('limit', limit.toString())

        const result = await $fetch<{
          success: boolean
          resources: MarketResource[]
        }>(`/api/market/popular?${params}`)

        return result.success ? result.resources : []
      } catch (error) {
        console.error('Failed to fetch popular resources:', error)
        return []
      }
    },

    async fetchLatest(type?: ResourceType, limit: number = 10) {
      try {
        const params = new URLSearchParams()
        if (type) params.set('type', type)
        params.set('limit', limit.toString())

        const result = await $fetch<{
          success: boolean
          resources: MarketResource[]
        }>(`/api/market/latest?${params}`)

        return result.success ? result.resources : []
      } catch (error) {
        console.error('Failed to fetch latest resources:', error)
        return []
      }
    },

    setFilters(filters: Partial<MarketState['filters']>) {
      Object.assign(this.filters, filters)
    },

    clearFilters() {
      this.filters = {
        keyword: '',
        type: null,
        license: null,
        tags: [],
        sortBy: 'DOWNLOADS',
        sortOrder: 'DESC',
      }
    },
  },
})
