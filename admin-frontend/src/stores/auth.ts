import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, AuthToken } from '../types/auth'
import { authApi } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<UserInfo | null>(null)
  const token = ref<AuthToken | null>(null)
  const loading = ref(false)

  // Getters
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN' || user.value?.role === 'SUPER_ADMIN')
  const permissions = computed(() => user.value?.permissions ?? [])

  // Actions
  async function login(username: string, password: string, remember: boolean = false) {
    loading.value = true
    try {
      const result = await authApi.login({ username, password, remember })
      if (result.success && result.user && result.token) {
        user.value = result.user
        token.value = result.token
        if (remember) {
          localStorage.setItem('auth_token', result.token.accessToken)
          localStorage.setItem('refresh_token', result.token.refreshToken)
        } else {
          sessionStorage.setItem('auth_token', result.token.accessToken)
          sessionStorage.setItem('refresh_token', result.token.refreshToken)
        }
        return { success: true }
      }
      return { success: false, error: result.error }
    } catch (error) {
      return { success: false, error: 'Login failed' }
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    if (token.value) {
      await authApi.logout(token.value.accessToken)
    }
    user.value = null
    token.value = null
    localStorage.removeItem('auth_token')
    localStorage.removeItem('refresh_token')
    sessionStorage.removeItem('auth_token')
    sessionStorage.removeItem('refresh_token')
  }

  async function refreshAuth() {
    const refreshToken = localStorage.getItem('refresh_token') || sessionStorage.getItem('refresh_token')
    if (!refreshToken) return false

    try {
      const result = await authApi.refresh(refreshToken)
      if (result.success && result.user && result.token) {
        user.value = result.user
        token.value = result.token
        return true
      }
    } catch {
      // Refresh failed
    }
    return false
  }

  async function checkAuth() {
    const accessToken = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token')
    if (!accessToken) return false

    try {
      const userInfo = await authApi.validateToken(accessToken)
      if (userInfo) {
        user.value = userInfo
        return true
      }
    } catch {
      // Token invalid, try refresh
      return await refreshAuth()
    }
    return false
  }

  function hasPermission(permission: string): boolean {
    if (user.value?.role === 'SUPER_ADMIN') return true
    return permissions.value.includes(permission)
  }

  return {
    user,
    token,
    loading,
    isAuthenticated,
    isAdmin,
    permissions,
    login,
    logout,
    refreshAuth,
    checkAuth,
    hasPermission
  }
})
