import { defineStore } from 'pinia'
import type { UserInfo, AuthToken, AuthResult } from '~/types'

interface AuthState {
  user: UserInfo | null
  token: AuthToken | null
  loading: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: null,
    loading: false,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'ADMIN' || state.user?.role === 'SUPER_ADMIN',
    isModerator: (state) => ['MODERATOR', 'ADMIN', 'SUPER_ADMIN'].includes(state.user?.role || ''),
  },

  actions: {
    async login(username: string, password: string, remember: boolean = false) {
      this.loading = true
      try {
        const result = await $fetch<AuthResult>('/api/auth/login', {
          method: 'POST',
          body: { username, password, remember },
        })

        if (result.success && result.user && result.token) {
          this.user = result.user
          this.token = result.token
          this.persistAuth()
          return { success: true }
        }
        return { success: false, error: result.error }
      } catch (error: any) {
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    async register(username: string, email: string, password: string, inviteCode?: string) {
      this.loading = true
      try {
        const result = await $fetch<AuthResult>('/api/auth/register', {
          method: 'POST',
          body: { username, email, password, inviteCode },
        })

        if (result.success && result.user && result.token) {
          this.user = result.user
          this.token = result.token
          this.persistAuth()
          return { success: true }
        }
        return { success: false, error: result.error }
      } catch (error: any) {
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    async logout() {
      if (this.token) {
        try {
          await $fetch('/api/auth/logout', {
            method: 'POST',
            headers: {
              Authorization: `Bearer ${this.token.accessToken}`,
            },
          })
        } catch (error) {
          // Ignore logout errors
        }
      }
      this.user = null
      this.token = null
      this.clearAuth()
    },

    async refreshToken() {
      if (!this.token?.refreshToken) return false

      try {
        const result = await $fetch<AuthResult>('/api/auth/refresh', {
          method: 'POST',
          body: { refreshToken: this.token.refreshToken },
        })

        if (result.success && result.token) {
          this.token = result.token
          this.persistAuth()
          return true
        }
        return false
      } catch (error) {
        this.logout()
        return false
      }
    },

    async validateToken() {
      if (!this.token) return false

      try {
        const result = await $fetch<{ success: boolean; user?: UserInfo }>('/api/auth/validate', {
          headers: {
            Authorization: `Bearer ${this.token.accessToken}`,
          },
        })

        if (result.success && result.user) {
          this.user = result.user
          return true
        }
        return false
      } catch (error) {
        return false
      }
    },

    persistAuth() {
      if (process.client) {
        localStorage.setItem('auth_token', JSON.stringify(this.token))
        localStorage.setItem('auth_user', JSON.stringify(this.user))
      }
    },

    clearAuth() {
      if (process.client) {
        localStorage.removeItem('auth_token')
        localStorage.removeItem('auth_user')
      }
    },

    restoreAuth() {
      if (process.client) {
        const tokenStr = localStorage.getItem('auth_token')
        const userStr = localStorage.getItem('auth_user')
        if (tokenStr && userStr) {
          try {
            this.token = JSON.parse(tokenStr)
            this.user = JSON.parse(userStr)
            // Validate token in background
            this.validateToken()
          } catch (error) {
            this.clearAuth()
          }
        }
      }
    },
  },
})
