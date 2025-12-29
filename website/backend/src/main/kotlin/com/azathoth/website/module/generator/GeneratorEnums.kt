package com.azathoth.website.module.generator

/**
 * 项目类型
 */
enum class ProjectType {
    GAME_PLUGIN,       // 游戏内容插件
    EXTENSION_PLUGIN,  // 扩展服务插件
    ADMIN_MODULE       // 管理后台模块
}

/**
 * 可选模块
 */
enum class FeatureModule {
    SKILL_SYSTEM,      // 技能系统
    DUNGEON_SYSTEM,    // 副本系统
    AI_BEHAVIOR,       // AI 行为
    ITEM_SYSTEM,       // 物品系统
    QUEST_SYSTEM,      // 任务系统
    COMMAND_SYSTEM,    // 命令系统
    DATABASE,          // 数据库集成
    REDIS,             // Redis 集成
    GRPC_CLIENT        // gRPC 客户端
}
