/**
 * 玩家信息
 */
export interface PlayerInfo {
  playerId: string
  username: string
  displayName: string
  level: number
  experience: number
  gold: number
  diamond: number
  vipLevel: number
  guildId?: string
  guildName?: string
  online: boolean
  currentInstance?: string
  lastLoginAt?: string
  createdAt: string
  banStatus?: BanStatus
}

/**
 * 封禁状态
 */
export interface BanStatus {
  banned: boolean
  reason?: string
  bannedAt?: string
  expiresAt?: string
  bannedBy?: string
}

/**
 * 玩家统计
 */
export interface PlayerStats {
  totalPlayers: number
  onlinePlayers: number
  newPlayersToday: number
  newPlayersThisWeek: number
  activePlayersToday: number
}

/**
 * 实例信息
 */
export interface InstanceInfo {
  instanceId: string
  instanceType: 'MAIN_CITY' | 'DUNGEON' | 'ARENA' | 'EVENT'
  templateId?: string
  templateName?: string
  state: 'CREATING' | 'WAITING' | 'IN_PROGRESS' | 'COMPLETED' | 'CLOSED'
  playerCount: number
  maxPlayers: number
  cpu: number
  memory: number
  createdAt: string
  region: string
}

/**
 * 实例统计
 */
export interface InstanceStats {
  totalInstances: number
  activeInstances: number
  cpuUsage: number
  memoryUsage: number
  byType: Record<string, number>
}

/**
 * 活动信息
 */
export interface ActivityInfo {
  activityId: string
  name: string
  description: string
  type: 'LIMITED_TIME' | 'RECURRING' | 'PERMANENT' | 'SPECIAL_EVENT' | 'SEASONAL'
  state: 'SCHEDULED' | 'PREPARING' | 'ACTIVE' | 'ENDING' | 'ENDED' | 'CANCELLED'
  startTime: string
  endTime: string
  participantCount: number
}

/**
 * 公告信息
 */
export interface AnnouncementInfo {
  announcementId: string
  title: string
  content: string
  type: 'NORMAL' | 'IMPORTANT' | 'MAINTENANCE' | 'EVENT'
  publishedAt?: string
  expiresAt?: string
  published: boolean
  authorId: string
  authorName: string
}

/**
 * 日志条目
 */
export interface LogEntry {
  timestamp: string
  level: 'DEBUG' | 'INFO' | 'WARN' | 'ERROR'
  logger: string
  message: string
  instanceId?: string
  playerId?: string
  stackTrace?: string
}

/**
 * 分析数据
 */
export interface AnalyticsData {
  timestamp: string
  onlinePlayers: number
  activeInstances: number
  transactions: number
  revenue: number
}

/**
 * 分页响应
 */
export interface PagedResponse<T> {
  items: T[]
  totalCount: number
  page: number
  pageSize: number
  totalPages: number
}

/**
 * API 响应
 */
export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
  message?: string
}
