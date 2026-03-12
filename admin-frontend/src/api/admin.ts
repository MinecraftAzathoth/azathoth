import type {
  PlayerInfo,
  PlayerStats,
  InstanceInfo,
  InstanceStats,
  ActivityInfo,
  AnnouncementInfo,
  LogEntry,
  AnalyticsData,
  PagedResponse,
} from '../types'

// --- Mock Data ---

const mockPlayers: PlayerInfo[] = [
  {
    playerId: 'p-001',
    username: 'DragonSlayer',
    displayName: '屠龙勇士',
    level: 85,
    experience: 234500,
    gold: 128000,
    diamond: 560,
    vipLevel: 5,
    guildId: 'g-001',
    guildName: '黎明之光',
    online: true,
    currentInstance: 'main-city-01',
    lastLoginAt: '2025-01-15T10:30:00Z',
    createdAt: '2024-06-01T08:00:00Z',
  },
  {
    playerId: 'p-002',
    username: 'ShadowMage',
    displayName: '暗影法师',
    level: 72,
    experience: 185200,
    gold: 95000,
    diamond: 320,
    vipLevel: 3,
    online: true,
    currentInstance: 'dungeon-fire-03',
    lastLoginAt: '2025-01-15T09:15:00Z',
    createdAt: '2024-07-15T12:00:00Z',
  },
  {
    playerId: 'p-003',
    username: 'IronGuard',
    displayName: '铁壁守卫',
    level: 60,
    experience: 120000,
    gold: 45000,
    diamond: 150,
    vipLevel: 1,
    online: false,
    lastLoginAt: '2025-01-14T22:00:00Z',
    createdAt: '2024-09-20T16:00:00Z',
    banStatus: { banned: false },
  },
  {
    playerId: 'p-004',
    username: 'WindRanger',
    displayName: '风行者',
    level: 91,
    experience: 310000,
    gold: 200000,
    diamond: 880,
    vipLevel: 7,
    guildId: 'g-002',
    guildName: '暗夜精灵',
    online: true,
    currentInstance: 'arena-01',
    lastLoginAt: '2025-01-15T11:00:00Z',
    createdAt: '2024-03-10T10:00:00Z',
  },
  {
    playerId: 'p-005',
    username: 'FrostQueen',
    displayName: '冰霜女王',
    level: 45,
    experience: 78000,
    gold: 32000,
    diamond: 90,
    vipLevel: 0,
    online: false,
    lastLoginAt: '2025-01-10T18:30:00Z',
    createdAt: '2024-11-05T14:00:00Z',
    banStatus: { banned: true, reason: '使用外挂', bannedAt: '2025-01-12T00:00:00Z', bannedBy: 'admin' },
  },
]

const mockInstances: InstanceInfo[] = [
  { instanceId: 'main-city-01', instanceType: 'MAIN_CITY', templateName: '主城-东方', state: 'IN_PROGRESS', playerCount: 342, maxPlayers: 500, cpu: 45, memory: 62, createdAt: '2025-01-15T00:00:00Z', region: 'cn-east' },
  { instanceId: 'main-city-02', instanceType: 'MAIN_CITY', templateName: '主城-西方', state: 'IN_PROGRESS', playerCount: 289, maxPlayers: 500, cpu: 38, memory: 55, createdAt: '2025-01-15T00:00:00Z', region: 'cn-west' },
  { instanceId: 'dungeon-fire-03', instanceType: 'DUNGEON', templateName: '烈焰深渊', state: 'IN_PROGRESS', playerCount: 5, maxPlayers: 10, cpu: 72, memory: 40, createdAt: '2025-01-15T09:00:00Z', region: 'cn-east' },
  { instanceId: 'arena-01', instanceType: 'ARENA', templateName: '竞技场-S1', state: 'WAITING', playerCount: 18, maxPlayers: 50, cpu: 15, memory: 20, createdAt: '2025-01-15T08:00:00Z', region: 'cn-east' },
  { instanceId: 'event-lunar-01', instanceType: 'EVENT', templateName: '春节活动副本', state: 'CREATING', playerCount: 0, maxPlayers: 100, cpu: 5, memory: 10, createdAt: '2025-01-15T11:00:00Z', region: 'cn-south' },
  { instanceId: 'dungeon-ice-02', instanceType: 'DUNGEON', templateName: '冰霜王座', state: 'COMPLETED', playerCount: 0, maxPlayers: 10, cpu: 0, memory: 5, createdAt: '2025-01-15T07:00:00Z', region: 'cn-north' },
]

const mockActivities: ActivityInfo[] = [
  { activityId: 'a-001', name: '春节庆典', description: '春节限时活动，丰厚奖励等你来拿', type: 'SEASONAL', state: 'ACTIVE', startTime: '2025-01-20T00:00:00Z', endTime: '2025-02-10T23:59:59Z', participantCount: 12500 },
  { activityId: 'a-002', name: '每日签到', description: '每日登录领取奖励', type: 'PERMANENT', state: 'ACTIVE', startTime: '2024-01-01T00:00:00Z', endTime: '2099-12-31T23:59:59Z', participantCount: 45000 },
  { activityId: 'a-003', name: '公会战', description: '每周六公会对抗赛', type: 'RECURRING', state: 'SCHEDULED', startTime: '2025-01-18T20:00:00Z', endTime: '2025-01-18T22:00:00Z', participantCount: 0 },
  { activityId: 'a-004', name: '限时Boss挑战', description: '击败世界Boss获取稀有装备', type: 'LIMITED_TIME', state: 'ENDING', startTime: '2025-01-14T10:00:00Z', endTime: '2025-01-15T10:00:00Z', participantCount: 8200 },
  { activityId: 'a-005', name: '新手引导活动', description: '新玩家专属福利', type: 'SPECIAL_EVENT', state: 'ENDED', startTime: '2024-12-01T00:00:00Z', endTime: '2025-01-01T00:00:00Z', participantCount: 3200 },
]

const mockAnnouncements: AnnouncementInfo[] = [
  { announcementId: 'ann-001', title: '服务器维护公告', content: '亲爱的玩家，服务器将于1月16日凌晨2:00-6:00进行例行维护，届时将无法登录游戏，请提前做好准备。', type: 'MAINTENANCE', publishedAt: '2025-01-15T10:00:00Z', published: true, authorId: 'admin-001', authorName: '系统管理员' },
  { announcementId: 'ann-002', title: '春节活动开启', content: '春节庆典活动将于1月20日正式开启！参与活动可获得限定坐骑、时装等丰厚奖励。', type: 'EVENT', publishedAt: '2025-01-14T12:00:00Z', published: true, authorId: 'admin-002', authorName: '运营团队' },
  { announcementId: 'ann-003', title: '版本更新说明 v2.5.0', content: '新增冰霜王座副本、竞技场赛季S2、公会系统优化等内容。详细更新日志请查看官网。', type: 'NORMAL', publishedAt: '2025-01-13T08:00:00Z', published: true, authorId: 'admin-001', authorName: '系统管理员' },
  { announcementId: 'ann-004', title: '严厉打击外挂行为', content: '近期发现部分玩家使用第三方外挂程序，我们将加大打击力度，一经发现永久封禁。', type: 'IMPORTANT', publishedAt: '2025-01-12T15:00:00Z', published: true, authorId: 'admin-003', authorName: '安全团队' },
]

const mockLogs: LogEntry[] = [
  { timestamp: '2025-01-15T11:30:45Z', level: 'INFO', logger: 'Gateway', message: '玩家 DragonSlayer 登录成功', playerId: 'p-001' },
  { timestamp: '2025-01-15T11:30:40Z', level: 'INFO', logger: 'GameInstance', message: '实例 main-city-01 玩家数: 342', instanceId: 'main-city-01' },
  { timestamp: '2025-01-15T11:30:35Z', level: 'WARN', logger: 'GameInstance', message: '实例 dungeon-fire-03 CPU 使用率超过 70%', instanceId: 'dungeon-fire-03' },
  { timestamp: '2025-01-15T11:30:30Z', level: 'ERROR', logger: 'TradeService', message: '交易处理失败: 库存不足', playerId: 'p-002', stackTrace: 'InsufficientInventoryException at TradeService.execute(TradeService.kt:142)' },
  { timestamp: '2025-01-15T11:30:25Z', level: 'INFO', logger: 'DungeonService', message: '副本 冰霜王座 已完成', instanceId: 'dungeon-ice-02' },
  { timestamp: '2025-01-15T11:30:20Z', level: 'WARN', logger: 'Gateway', message: '连接超时: 客户端 192.168.1.xxx' },
  { timestamp: '2025-01-15T11:30:15Z', level: 'INFO', logger: 'ActivityService', message: '活动 限时Boss挑战 即将结束' },
  { timestamp: '2025-01-15T11:30:10Z', level: 'ERROR', logger: 'ChatService', message: '消息发送失败: Kafka 连接中断' },
  { timestamp: '2025-01-15T11:30:05Z', level: 'INFO', logger: 'PlayerService', message: '玩家 FrostQueen 已被封禁', playerId: 'p-005' },
  { timestamp: '2025-01-15T11:30:00Z', level: 'DEBUG', logger: 'GameInstance', message: '实例 event-lunar-01 创建中', instanceId: 'event-lunar-01' },
]

const mockAnalytics: AnalyticsData[] = Array.from({ length: 24 }, (_, i) => ({
  timestamp: `2025-01-15T${String(i).padStart(2, '0')}:00:00Z`,
  onlinePlayers: Math.floor(800 + Math.sin(i / 3.8) * 400 + Math.random() * 100),
  activeInstances: Math.floor(15 + Math.sin(i / 3.8) * 8 + Math.random() * 3),
  transactions: Math.floor(200 + Math.random() * 150),
  revenue: Math.floor(5000 + Math.random() * 3000),
}))

// --- Mock API Functions ---

function delay(ms = 300): Promise<void> {
  return new Promise((r) => setTimeout(r, ms))
}

export async function fetchPlayerStats(): Promise<PlayerStats> {
  await delay()
  return {
    totalPlayers: 52340,
    onlinePlayers: 1247,
    newPlayersToday: 186,
    newPlayersThisWeek: 1023,
    activePlayersToday: 4520,
  }
}

export async function fetchInstanceStats(): Promise<InstanceStats> {
  await delay()
  return {
    totalInstances: 48,
    activeInstances: 32,
    cpuUsage: 42,
    memoryUsage: 58,
    byType: { MAIN_CITY: 8, DUNGEON: 18, ARENA: 4, EVENT: 2 },
  }
}

export async function fetchPlayers(page = 1, pageSize = 10, search = ''): Promise<PagedResponse<PlayerInfo>> {
  await delay()
  let filtered = mockPlayers
  if (search) {
    const q = search.toLowerCase()
    filtered = mockPlayers.filter((p) => p.username.toLowerCase().includes(q) || p.displayName.includes(q))
  }
  return {
    items: filtered.slice((page - 1) * pageSize, page * pageSize),
    totalCount: filtered.length,
    page,
    pageSize,
    totalPages: Math.ceil(filtered.length / pageSize),
  }
}

export async function fetchPlayer(id: string): Promise<PlayerInfo | null> {
  await delay()
  return mockPlayers.find((p) => p.playerId === id) ?? null
}

export async function fetchInstances(): Promise<InstanceInfo[]> {
  await delay()
  return mockInstances
}

export async function fetchActivities(): Promise<ActivityInfo[]> {
  await delay()
  return mockActivities
}

export async function fetchAnnouncements(): Promise<AnnouncementInfo[]> {
  await delay()
  return mockAnnouncements
}

export async function fetchLogs(level?: string): Promise<LogEntry[]> {
  await delay()
  if (level && level !== 'ALL') {
    return mockLogs.filter((l) => l.level === level)
  }
  return mockLogs
}

export async function fetchAnalytics(): Promise<AnalyticsData[]> {
  await delay()
  return mockAnalytics
}
