import type { ApiResponse } from '~/types'

interface FetchOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: any
  query?: Record<string, any>
  headers?: Record<string, string>
}

/**
 * API 客户端 composable
 * 提供带认证的 HTTP 请求方法
 */
export function useApi() {
  const authStore = useAuthStore()
  const config = useRuntimeConfig()

  const baseURL = config.public.apiBase || '/api'

  /**
   * 获取认证头
   */
  function getAuthHeaders(): Record<string, string> {
    if (authStore.token?.accessToken) {
      return {
        Authorization: `Bearer ${authStore.token.accessToken}`,
      }
    }
    return {}
  }

  /**
   * 通用请求方法
   */
  async function request<T>(
    endpoint: string,
    options: FetchOptions = {}
  ): Promise<ApiResponse<T>> {
    const { method = 'GET', body, query, headers = {} } = options

    try {
      const response = await $fetch<ApiResponse<T>>(`${baseURL}${endpoint}`, {
        method,
        body,
        query,
        headers: {
          ...getAuthHeaders(),
          ...headers,
        },
        onResponseError({ response }) {
          // 处理 401 错误，尝试刷新 token
          if (response.status === 401 && authStore.token?.refreshToken) {
            authStore.refreshToken()
          }
        },
      })

      return response
    } catch (error: any) {
      return {
        success: false,
        error: {
          code: 'REQUEST_FAILED',
          message: error.message || '请求失败',
        },
      }
    }
  }

  /**
   * GET 请求
   */
  async function get<T>(
    endpoint: string,
    query?: Record<string, any>
  ): Promise<ApiResponse<T>> {
    return request<T>(endpoint, { method: 'GET', query })
  }

  /**
   * POST 请求
   */
  async function post<T>(
    endpoint: string,
    body?: any
  ): Promise<ApiResponse<T>> {
    return request<T>(endpoint, { method: 'POST', body })
  }

  /**
   * PUT 请求
   */
  async function put<T>(
    endpoint: string,
    body?: any
  ): Promise<ApiResponse<T>> {
    return request<T>(endpoint, { method: 'PUT', body })
  }

  /**
   * PATCH 请求
   */
  async function patch<T>(
    endpoint: string,
    body?: any
  ): Promise<ApiResponse<T>> {
    return request<T>(endpoint, { method: 'PATCH', body })
  }

  /**
   * DELETE 请求
   */
  async function del<T>(endpoint: string): Promise<ApiResponse<T>> {
    return request<T>(endpoint, { method: 'DELETE' })
  }

  /**
   * 上传文件 (multipart/form-data)
   */
  async function upload<T>(
    endpoint: string,
    formData: FormData
  ): Promise<ApiResponse<T>> {
    try {
      const response = await $fetch<ApiResponse<T>>(`${baseURL}${endpoint}`, {
        method: 'POST',
        body: formData,
        headers: {
          ...getAuthHeaders(),
        },
      })

      return response
    } catch (error: any) {
      return {
        success: false,
        error: {
          code: 'UPLOAD_FAILED',
          message: error.message || '上传失败',
        },
      }
    }
  }

  return {
    request,
    get,
    post,
    put,
    patch,
    del,
    upload,
    getAuthHeaders,
  }
}

/**
 * 市场 API 专用 composable
 */
export function useMarketApi() {
  const api = useApi()

  // ========== 资源相关 ==========

  async function searchResources(params: {
    keyword?: string
    type?: string
    license?: string
    minRating?: number
    tags?: string[]
    sortBy?: string
    sortOrder?: string
    page?: number
    pageSize?: number
  }) {
    const query: Record<string, any> = {
      page: params.page || 1,
      pageSize: params.pageSize || 20,
    }

    if (params.keyword) query.keyword = params.keyword
    if (params.type) query.type = params.type
    if (params.license) query.license = params.license
    if (params.minRating) query.minRating = params.minRating
    if (params.tags?.length) query.tags = params.tags.join(',')
    if (params.sortBy) query.sortBy = params.sortBy
    if (params.sortOrder) query.sortOrder = params.sortOrder

    return api.get('/market/resources', query)
  }

  async function getResource(resourceId: string) {
    return api.get(`/market/resources/${resourceId}`)
  }

  async function getResourceBySlug(slug: string) {
    return api.get(`/market/resources/slug/${slug}`)
  }

  async function getPopularResources(type?: string, limit: number = 10) {
    const query: Record<string, any> = { limit }
    if (type) query.type = type
    return api.get('/market/popular', query)
  }

  async function getLatestResources(type?: string, limit: number = 10) {
    const query: Record<string, any> = { limit }
    if (type) query.type = type
    return api.get('/market/latest', query)
  }

  async function getUserResources(userId: string) {
    return api.get(`/market/users/${userId}/resources`)
  }

  async function createResource(data: {
    name: string
    description: string
    type: string
    license: string
    pricing?: {
      price: number
      currency: string
      subscriptionPeriod?: number
    }
    minApiVersion: string
    dependencies: string[]
    tags: string[]
  }) {
    return api.post('/market/resources', data)
  }

  async function updateResource(
    resourceId: string,
    data: {
      name?: string
      description?: string
      pricing?: {
        price: number
        currency: string
        subscriptionPeriod?: number
      }
      icon?: string
      screenshots?: string[]
      tags?: string[]
    }
  ) {
    return api.patch(`/market/resources/${resourceId}`, data)
  }

  async function deleteResource(resourceId: string) {
    return api.del(`/market/resources/${resourceId}`)
  }

  async function publishVersion(
    resourceId: string,
    data: {
      version: string
      changelog: string
      minApiVersion: string
      file: File
    }
  ) {
    const formData = new FormData()
    formData.append('version', data.version)
    formData.append('changelog', data.changelog)
    formData.append('minApiVersion', data.minApiVersion)
    formData.append('file', data.file)

    return api.upload(`/market/resources/${resourceId}/versions`, formData)
  }

  // ========== 评论相关 ==========

  async function getReviews(
    resourceId: string,
    page: number = 1,
    pageSize: number = 20
  ) {
    return api.get(`/market/resources/${resourceId}/reviews`, { page, pageSize })
  }

  async function createReview(
    resourceId: string,
    data: { rating: number; content: string }
  ) {
    return api.post(`/market/resources/${resourceId}/reviews`, data)
  }

  async function replyToReview(reviewId: string, reply: string) {
    return api.post(`/market/reviews/${reviewId}/reply`, { reply })
  }

  async function markReviewHelpful(reviewId: string) {
    return api.post(`/market/reviews/${reviewId}/helpful`)
  }

  return {
    // 资源
    searchResources,
    getResource,
    getResourceBySlug,
    getPopularResources,
    getLatestResources,
    getUserResources,
    createResource,
    updateResource,
    deleteResource,
    publishVersion,
    // 评论
    getReviews,
    createReview,
    replyToReview,
    markReviewHelpful,
  }
}
