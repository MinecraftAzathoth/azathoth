package com.azathoth.game.dungeons.template

import com.azathoth.game.dungeons.instance.DungeonDifficulty
import kotlin.time.Duration

/**
 * 副本模板
 */
interface DungeonTemplate {
    /** 模板ID */
    val templateId: String
    
    /** 副本名称 */
    val name: String
    
    /** 副本描述 */
    val description: String
    
    /** 最小玩家数 */
    val minPlayers: Int
    
    /** 最大玩家数 */
    val maxPlayers: Int
    
    /** 推荐等级 */
    val recommendedLevel: Int
    
    /** 最低等级要求 */
    val minLevel: Int
    
    /** 支持的难度 */
    val supportedDifficulties: Set<DungeonDifficulty>
    
    /** 时间限制 */
    fun getTimeLimit(difficulty: DungeonDifficulty): Duration?
    
    /** 世界模板名称 */
    val worldTemplateName: String
    
    /** 出生点配置 */
    val spawnPointConfig: SpawnPointConfig
    
    /** 阶段配置 */
    val phases: List<PhaseConfig>
    
    /** 奖励配置 */
    fun getRewardConfig(difficulty: DungeonDifficulty): RewardConfig
    
    /** 每日进入次数限制 */
    val dailyEntryLimit: Int
    
    /** 每周进入次数限制 */
    val weeklyEntryLimit: Int
    
    /** 冷却时间 */
    val cooldown: Duration
}

/**
 * 出生点配置
 */
interface SpawnPointConfig {
    /** 玩家出生点 */
    val playerSpawnPoints: List<SpawnPoint>
    
    /** Boss 出生点 */
    val bossSpawnPoints: List<SpawnPoint>
    
    /** 怪物出生点 */
    val mobSpawnPoints: List<SpawnPoint>
}

/**
 * 出生点
 */
data class SpawnPoint(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
    val tag: String = ""
)

/**
 * 阶段配置
 */
interface PhaseConfig {
    /** 阶段ID */
    val phaseId: String
    
    /** 阶段名称 */
    val name: String
    
    /** 阶段顺序 */
    val order: Int
    
    /** 目标配置 */
    val objectives: List<ObjectiveConfig>
    
    /** 怪物波次配置 */
    val waves: List<WaveConfig>
    
    /** 阶段开始触发器 */
    val startTriggers: List<TriggerConfig>
    
    /** 阶段完成触发器 */
    val completeTriggers: List<TriggerConfig>
}

/**
 * 目标配置
 */
interface ObjectiveConfig {
    /** 目标ID */
    val objectiveId: String
    
    /** 目标类型 */
    val type: ObjectiveType
    
    /** 目标描述 */
    val description: String
    
    /** 目标数量 */
    val targetCount: Int
    
    /** 是否必须完成 */
    val required: Boolean
}

/**
 * 目标类型
 */
enum class ObjectiveType {
    /** 击杀怪物 */
    KILL_MOBS,
    /** 击杀 Boss */
    KILL_BOSS,
    /** 到达位置 */
    REACH_LOCATION,
    /** 收集物品 */
    COLLECT_ITEMS,
    /** 保护 NPC */
    PROTECT_NPC,
    /** 存活一定时间 */
    SURVIVE_TIME,
    /** 激活机关 */
    ACTIVATE_MECHANISM,
    /** 自定义 */
    CUSTOM
}

/**
 * 怪物波次配置
 */
interface WaveConfig {
    /** 波次ID */
    val waveId: String
    
    /** 波次顺序 */
    val order: Int
    
    /** 怪物配置 */
    val mobs: List<MobSpawnConfig>
    
    /** 波次间隔（秒） */
    val delayAfterPrevious: Int
    
    /** 是否 Boss 波次 */
    val isBossWave: Boolean
}

/**
 * 怪物生成配置
 */
interface MobSpawnConfig {
    /** 怪物类型 */
    val mobType: String
    
    /** 生成数量 */
    val count: Int
    
    /** 等级 */
    val level: Int
    
    /** 生成点标签 */
    val spawnPointTag: String
    
    /** 属性修改器 */
    val modifiers: Map<String, Double>
}

/**
 * 触发器配置
 */
interface TriggerConfig {
    /** 触发器类型 */
    val type: String
    
    /** 触发器参数 */
    val parameters: Map<String, Any>
}

/**
 * 奖励配置
 */
interface RewardConfig {
    /** 经验奖励 */
    val experience: Int
    
    /** 金币奖励 */
    val gold: Int
    
    /** 固定物品奖励 */
    val fixedItems: List<ItemReward>
    
    /** 随机物品池 */
    val randomItemPool: List<RandomItemReward>
    
    /** 首通额外奖励 */
    val firstClearBonus: List<ItemReward>
}

/**
 * 物品奖励
 */
interface ItemReward {
    /** 物品ID */
    val itemId: String
    
    /** 数量 */
    val amount: Int
}

/**
 * 随机物品奖励
 */
interface RandomItemReward {
    /** 物品ID */
    val itemId: String
    
    /** 数量范围 */
    val amountRange: IntRange
    
    /** 掉落概率 */
    val dropRate: Double
}
