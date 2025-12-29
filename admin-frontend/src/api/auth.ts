import axios from 'axios'
import type { LoginRequest, AuthResult, UserInfo } from '../types/auth'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token 过期，尝试刷新
      const refreshToken = localStorage.getItem('refresh_token') || sessionStorage.getItem('refresh_token')
      if (refreshToken) {
        try {
          const result = await authApi.refresh(refreshToken)
          if (result.success && result.token) {
            // 更新 token
            const storage = localStorage.getItem('auth_token') ? localStorage : sessionStorage
            storage.setItem('auth_token', result.token.accessToken)
            storage.setItem('refresh_token', result.token.refreshToken)
            // 重试原请求
            error.config.headers.Authorization = `Bearer ${result.token.accessToken}`
            return api.request(error.config)
          }
        } catch {
          // 刷新失败，跳转登录
        }
      }
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  async login(request: LoginRequest): Promise<AuthResult> {
    const response = await api.post<AuthResult>('/auth/login', request)
    return response.data
  },

  async logout(accessToken: string): Promise<void> {
    await api.post('/auth/logout', null, {
      headers: { Authorization: `Bearer ${accessToken}` }
    })
  },

  async refresh(refreshToken: string): Promise<AuthResult> {
    const response = await api.post<AuthResult>('/auth/refresh', { refreshToken })
    return response.data
  },

  async validateToken(accessToken: string): Promise<UserInfo | null> {
    try {
      const response = await api.get<UserInfo>('/auth/me', {
        headers: { Authorization: `Bearer ${accessToken}` }
      })
      return response.data
    } catch {
      return null
    }
  }
}

export { api }
