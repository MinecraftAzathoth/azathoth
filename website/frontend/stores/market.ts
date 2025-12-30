import { defineStore } from 'pinia'
import type { MarketResource, ResourceType, LicenseType, ResourceReview, Pagination } from '~/types'

interface MarketState {
  resources: MarketResource[]
  currentResource: MarketResource | null
  myResources: MarketResource[]
  reviews: ResourceReview[]
  pagination: Pagination | null
  reviewsPagination: Pagination | null
  loading: boolean
  submitting: boolean
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
    myResources: [],
    reviews: [],
    pagination: null,
    reviewsPagination: null,
    loading: false,
    submitting: false,
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

    // ========== 用户资源管理 ==========

    async fetchMyResources() {
      const authStore = useAuthStore()
      if (!authStore.user) return

      this.loading = true
      try {
        const result = await $fetch<{
          success: boolean
          resources: MarketResource[]
        }>(`/api/market/users/${authStore.user.userId}/resources`, {
          headers: {
            Authorization: `Bearer ${authStore.token?.accessToken}`,
          },
        })

        if (result.success) {
          this.myResources = result.resources
        }
      } catch (error) {
        console.error('Failed to fetch my resources:', error)
      } finally {
        this.loading = false
      }
    },

    async createResource(data: {
      name: string
      description: string
      type: string
      license: string
      pricing?: { price: number; currency: string; subscriptionPeriod?: number }
      minApiVersion: string
      dependencies: string[]
      tags: string[]
    }) {
      const authStore = useAuthStore()
      if (!authStore.token) return { success: false, error: '请先登录' }

      this.submitting = true
      try {
        const result = await $fetch<{
          success: boolean
          resource?: MarketResource
          error?: { code: string; message: string }
        }>('/api/market/resources', {
          method: 'POST',
          body: data,
          headers: {
            Authorization: `Bearer ${authStore.token.accessToken}`,
          },
        })

        if (result.success && result.resource) {
          this.myResources.unshift(result.resource)
          return { success: true, resource: result.resource }
        }
        return { success: false, error: result.error?.message || '创建失败' }
      } catch (error: any) {
        return { success: false, error: error.message }
      } finally {
        this.submitting = false
      }
    },

    async updateResource(resourceId: string, data: {
      name?: string
      description?: string
      pricing?: { price: number; currency: string; subscriptionPeriod?: number }
      icon?: string
      screenshots?: string[]
      tags?: string[]
    }) {
      const authStore = useAuthStore()
      if (!authStore.token) return { success: false, error: '请先登录' }

      this.submitting = true
      try {
        const result = await $fetch<{
          success: boolean
          resource?: MarketResource
          error?: { code: string; message: string }
        }>(`/api/market/resources/${resourceId}`, {
          method: 'PATCH',
          body: data,
          headers: {
            Authorization: `Bearer ${authStore.token.accessToken}`,
          },
        })

        if (result.success && result.resource) {
          // 更新本地缓存
          const index = this.myResources.findIndex((r) => r.resourceId === resourceId)
          if (index !== -1) {
            this.myResources[index] = result.resource
          }
          if (this.currentResource?.resourceId === resourceId) {
            this.currentResource = result.resource
          }
          return { success: true, resource: result.resource }
        }
        return { success: false, error: result.error?.message || '更新失败' }
      } catch (error: any) {
        return { success: false, error: error.message }
      } finally {
        this.submitting = false
      }
    },

    async deleteResource(resourceId: string) {
      const authStore = useAuthStore()
      if (!authStore.token) return { success: false, error: '请先登录' }

      this.submitting = true
      try {
        const result = await $fetch<{ success: boolean }>(`/api/market/resources/${resourceId}`, {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${authStore.token.accessToken}`,
          },
        })

        if (result.success) {
          this.myResources = this.myResources.filter((r) => r.resourceId !== resourceId)
          return { success: true }
        }
        return { success: false, error: '删除失败' }
      } catch (error: any) {
        return { success: false, error: error.message }
      } finally {
        this.submitting = false
      }
    },

    async publishVersion(resourceId: string, data: {
      version: string
      changelog: string
      minApiVersion: string
      file: File
    }) {
      const authStore = useAuthStore()
      if (!authStore.token) return { success: false, error: '请先登录' }

      this.submitting = true
      try {
        const formData = new FormData()
        formData.append('version', data.version)
        formData.append('changelog', data.changelog)
        formData.append('minApiVersion', data.minApiVersion)
        formData.append('file', data.file)

        const result = await $fetch<{
          success: boolean
          version?: { version: string; changelog: string; downloadUrl: string }
          error?: { code: string; message: string }
        }>(`/api/market/resources/${resourceId}/versions`, {
          method: 'POST',
          body: formData,
          headers: {
            Authorization: `Bearer ${authStore.token.accessToken}`,
          },
        })

        if (result.success) {
          // 刷新资源详情
          await this.fetchResource(resourceId)
          return { success: true, version: result.version }
        }
        return { success: false, error: result.error?.message || '发布失败' }
      } catch (error: any) {
        return { success: false, error: error.message }
      } finally {
        this.submitting = false
      }
    },

    // ========== 评论管理 ==========

    async fetchReviews(resourceId: string, page: number = 1, pageSize: number = 20) {
      this.loading = true
      try {
        const result = await $fetch<{
          success: boolean
          reviews: ResourceReview[]
          pagination: Pagination
        }>(`/api/market/resources/${resourceId}/reviews?page=${page}&pageSize=${pageSize}`)

        if (result.success) {
          this.reviews = result.reviews
          this.reviewsPagination = result.pagination
        }
      } catch (error) {
        console.error('Failed to fetch reviews:', error)
      } finally {
        this.loading = false
      }
    },

    async createReview(resourceId: string, rating: number, content: string) {
      const authStore = useAuthStore()
      if (!authStore.token) return { success: false, error: '请先登录' }

      this.submitting = true
      try {
        const result = await $fetch<{
          success: boolean
          review?: ResourceReview
          error?: { code: string; message: string }
        }>(`/api/market/resources/${resourceId}/reviews`, {
          method: 'POST',
          body: { rating, content },
          headers: {
            Authorization: `Bearer ${authStore.token.accessToken}`,
          },
        })

        if (result.success && result.review) {
          this.reviews.unshift(result.review)
          return { success: true, review: result.review }
        }
        return { success: false, error: result.error?.message || '评论失败' }
      } catch (error: any) {
        return { success: false, error: error.message }
      } finally {
        this.submitting = false
      }
    },

    async replyToReview(reviewId: string, reply: string) {
      const authStore = useAuthStore()
      if (!authStore.token) return { success: false, error: '请先登录' }

      this.submitting = true
      try {
        const result = await $fetch<{
          success: boolean
          review?: ResourceReview
          error?: { code: string; message: string }
        }>(`/api/market/reviews/${reviewId}/reply`, {
          method: 'POST',
          body: { reply },
          headers: {
            Authorization: `Bearer ${authStore.token.accessToken}`,
          },
        })

        if (result.success && result.review) {
          const index = this.reviews.findIndex((r) => r.reviewId === reviewId)
          if (index !== -1) {
            this.reviews[index] = result.review
          }
          return { success: true }
        }
        return { success: false, error: result.error?.message || '回复失败' }
      } catch (error: any) {
        return { success: false, error: error.message }
      } finally {
        this.submitting = false
      }
    },

    async markReviewHelpful(reviewId: string) {
      const authStore = useAuthStore()
      if (!authStore.token) return { success: false, error: '请先登录' }

      try {
        const result = await $fetch<{ success: boolean }>(`/api/market/reviews/${reviewId}/helpful`, {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${authStore.token.accessToken}`,
          },
        })

        if (result.success) {
          const review = this.reviews.find((r) => r.reviewId === reviewId)
          if (review) {
            review.helpful += 1
          }
        }
        return result
      } catch (error: any) {
        return { success: false, error: error.message }
      }
    },
  },
})
