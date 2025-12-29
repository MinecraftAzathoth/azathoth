/**
 * 用户角色
 */
export type UserRole = 'USER' | 'DEVELOPER' | 'MODERATOR' | 'ADMIN' | 'SUPER_ADMIN'

/**
 * 用户信息
 */
export interface UserInfo {
  userId: string
  username: string
  email: string
  avatarUrl?: string
  role: UserRole
  permissions: string[]
  verified: boolean
  createdAt: string
  lastLoginAt?: string
}

/**
 * 认证令牌
 */
export interface AuthToken {
  accessToken: string
  refreshToken: string
  expiresAt: string
  tokenType: string
}

/**
 * 登录请求
 */
export interface LoginRequest {
  username: string
  password: string
  remember: boolean
}

/**
 * 认证结果
 */
export interface AuthResult {
  success: boolean
  user?: UserInfo
  token?: AuthToken
  error?: string
}
