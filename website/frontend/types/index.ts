// 用户相关类型
export enum UserRole {
  USER = 'USER',
  DEVELOPER = 'DEVELOPER',
  MODERATOR = 'MODERATOR',
  ADMIN = 'ADMIN',
  SUPER_ADMIN = 'SUPER_ADMIN'
}

export interface UserInfo {
  userId: string
  username: string
  email: string
  avatarUrl?: string
  role: UserRole
  verified: boolean
  createdAt: string
  lastLoginAt?: string
}

export interface AuthToken {
  accessToken: string
  refreshToken: string
  expiresAt: string
  tokenType: string
}

export interface AuthResult {
  success: boolean
  user?: UserInfo
  token?: AuthToken
  error?: string
}

export interface ProfileUpdate {
  displayName?: string
  bio?: string
  avatarUrl?: string
  website?: string
  github?: string
}

// 市场相关类型
export enum ResourceType {
  PLUGIN = 'PLUGIN',
  MODULE = 'MODULE',
  SERVICE = 'SERVICE',
  TEMPLATE = 'TEMPLATE',
  THEME = 'THEME',
  TOOL = 'TOOL'
}

export enum LicenseType {
  FREE_OPEN_SOURCE = 'FREE_OPEN_SOURCE',
  FREE_CLOSED_SOURCE = 'FREE_CLOSED_SOURCE',
  PAID_PERPETUAL = 'PAID_PERPETUAL',
  PAID_SUBSCRIPTION = 'PAID_SUBSCRIPTION'
}

export enum ResourceStatus {
  DRAFT = 'DRAFT',
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  SUSPENDED = 'SUSPENDED'
}

export interface ResourcePricing {
  price: number
  currency: string
  subscriptionPeriod?: number
}

export interface ResourceVersion {
  version: string
  changelog: string
  downloadUrl: string
  fileSize: number
  minApiVersion: string
  releasedAt: string
}

export interface MarketResource {
  resourceId: string
  name: string
  slug: string
  description: string
  type: ResourceType
  license: LicenseType
  authorId: string
  authorName: string
  versions: ResourceVersion[]
  latestVersion: string
  pricing?: ResourcePricing
  downloads: number
  rating: number
  reviewCount: number
  status: ResourceStatus
  minApiVersion: string
  maxApiVersion?: string
  dependencies: string[]
  icon: string
  screenshots: string[]
  tags: string[]
  createdAt: string
  updatedAt: string
}

export interface ResourceReview {
  reviewId: string
  resourceId: string
  userId: string
  userName: string
  rating: number
  content: string
  createdAt: string
  updatedAt?: string
  helpful: number
  authorReply?: string
}

// 支付相关类型
export enum PaymentChannel {
  WECHAT = 'WECHAT',
  ALIPAY = 'ALIPAY'
}

export enum OrderStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED',
  EXPIRED = 'EXPIRED'
}

export interface Order {
  orderId: string
  userId: string
  resourceId: string
  resourceName: string
  amount: number
  currency: string
  channel?: PaymentChannel
  status: OrderStatus
  paymentUrl?: string
  paidAt?: string
  createdAt: string
  expiresAt: string
}

export interface Balance {
  userId: string
  available: number
  frozen: number
  total: number
  currency: string
}

// 论坛相关类型
export enum PostSortBy {
  LATEST = 'LATEST',
  HOT = 'HOT',
  FEATURED = 'FEATURED',
  REPLIES = 'REPLIES'
}

export interface ForumCategory {
  categoryId: string
  name: string
  description: string
  icon: string
  order: number
  postCount: number
  lastPostAt?: string
}

export interface ForumPost {
  postId: string
  title: string
  content: string
  authorId: string
  authorName: string
  authorAvatar?: string
  categoryId: string
  tags: string[]
  isPinned: boolean
  isFeatured: boolean
  isLocked: boolean
  viewCount: number
  likeCount: number
  replyCount: number
  createdAt: string
  updatedAt: string
  lastReplyAt?: string
}

export interface ForumReply {
  replyId: string
  postId: string
  parentId?: string
  content: string
  authorId: string
  authorName: string
  authorAvatar?: string
  likeCount: number
  floor: number
  createdAt: string
  updatedAt?: string
}

// 生成器相关类型
export enum ProjectType {
  GAME_PLUGIN = 'GAME_PLUGIN',
  EXTENSION_PLUGIN = 'EXTENSION_PLUGIN',
  ADMIN_MODULE = 'ADMIN_MODULE'
}

export enum FeatureModule {
  SKILL_SYSTEM = 'SKILL_SYSTEM',
  DUNGEON_SYSTEM = 'DUNGEON_SYSTEM',
  AI_BEHAVIOR = 'AI_BEHAVIOR',
  ITEM_SYSTEM = 'ITEM_SYSTEM',
  QUEST_SYSTEM = 'QUEST_SYSTEM',
  COMMAND_SYSTEM = 'COMMAND_SYSTEM',
  DATABASE = 'DATABASE',
  REDIS = 'REDIS',
  GRPC_CLIENT = 'GRPC_CLIENT'
}

export interface ProjectConfig {
  projectName: string
  groupId: string
  version: string
  description: string
  author: string
  projectType: ProjectType
  features: FeatureModule[]
  kotlinVersion: string
  javaVersion: number
  includeExamples: boolean
  includeTests: boolean
}

export interface TemplateInfo {
  templateId: string
  name: string
  description: string
  projectType: ProjectType
  defaultFeatures: FeatureModule[]
  preview: string
}

// 通用类型
export interface Pagination {
  page: number
  pageSize: number
  totalCount: number
  totalPages: number
}

export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: {
    code: string
    message: string
  }
  pagination?: Pagination
}

export interface Notification {
  notificationId: string
  userId: string
  type: string
  title: string
  content: string
  link?: string
  read: boolean
  createdAt: string
}
