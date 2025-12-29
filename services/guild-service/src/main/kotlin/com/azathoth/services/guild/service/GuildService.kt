package com.azathoth.services.guild.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result

/**
 * 公会等级
 */
interface GuildLevel {
    /** 等级 */
    val level: Int
    
    /** 最大成员数 */
    val maxMembers: Int
    
    /** 升级所需经验 */
    val experienceRequired: Long
    
    /** 解锁功能 */
    val unlockedFeatures: Set<String>
}

/**
 * 公会职位
 */
enum class GuildRank(val priority: Int) {
    /** 会长 */
    LEADER(100),
    /** 副会长 */
    CO_LEADER(80),
    /** 长老 */
    ELDER(60),
    /** 成员 */
    MEMBER(40),
    /** 新人 */
    NEWCOMER(20)
}

/**
 * 公会信息
 */
interface Guild {
    /** 公会ID */
    val guildId: String
    
    /** 公会名称 */
    var name: String
    
    /** 公会标签 */
    var tag: String
    
    /** 公会公告 */
    var announcement: String
    
    /** 公会描述 */
    var description: String
    
    /** 公会等级 */
    val level: Int
    
    /** 公会经验 */
    val experience: Long
    
    /** 会长ID */
    val leaderId: PlayerId
    
    /** 成员数量 */
    val memberCount: Int
    
    /** 最大成员数 */
    val maxMembers: Int
    
    /** 公会资金 */
    var funds: Long
    
    /** 创建时间 */
    val createdAt: Long
    
    /** 是否招募中 */
    var recruiting: Boolean
    
    /** 加入要求等级 */
    var requiredLevel: Int
    
    /** 是否需要申请 */
    var requireApplication: Boolean
}

/**
 * 公会成员
 */
interface GuildMember {
    /** 玩家ID */
    val playerId: PlayerId
    
    /** 公会ID */
    val guildId: String
    
    /** 职位 */
    var rank: GuildRank
    
    /** 贡献值 */
    var contribution: Long
    
    /** 本周贡献 */
    var weeklyContribution: Long
    
    /** 加入时间 */
    val joinedAt: Long
    
    /** 最后活跃时间 */
    var lastActiveAt: Long
    
    /** 备注 */
    var note: String?
}

/**
 * 公会申请
 */
interface GuildApplication {
    /** 申请ID */
    val applicationId: String
    
    /** 玩家ID */
    val playerId: PlayerId
    
    /** 公会ID */
    val guildId: String
    
    /** 申请消息 */
    val message: String
    
    /** 申请时间 */
    val appliedAt: Long
    
    /** 申请状态 */
    val status: ApplicationStatus
}

/**
 * 申请状态
 */
enum class ApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}

/**
 * 公会服务
 */
interface GuildService {
    /**
     * 创建公会
     */
    suspend fun createGuild(
        leaderId: PlayerId,
        name: String,
        tag: String
    ): Result<Guild>
    
    /**
     * 解散公会
     */
    suspend fun disbandGuild(guildId: String, operatorId: PlayerId): Result<Unit>
    
    /**
     * 获取公会信息
     */
    suspend fun getGuild(guildId: String): Guild?
    
    /**
     * 获取玩家的公会
     */
    suspend fun getPlayerGuild(playerId: PlayerId): Guild?
    
    /**
     * 搜索公会
     */
    suspend fun searchGuilds(keyword: String, limit: Int = 20): List<Guild>
    
    /**
     * 申请加入公会
     */
    suspend fun applyToGuild(
        playerId: PlayerId,
        guildId: String,
        message: String = ""
    ): Result<GuildApplication>
    
    /**
     * 处理申请
     */
    suspend fun handleApplication(
        applicationId: String,
        approved: Boolean,
        operatorId: PlayerId
    ): Result<Unit>
    
    /**
     * 邀请玩家
     */
    suspend fun invitePlayer(
        guildId: String,
        inviterId: PlayerId,
        inviteeId: PlayerId
    ): Result<Unit>
    
    /**
     * 踢出成员
     */
    suspend fun kickMember(
        guildId: String,
        operatorId: PlayerId,
        targetId: PlayerId,
        reason: String = ""
    ): Result<Unit>
    
    /**
     * 离开公会
     */
    suspend fun leaveGuild(playerId: PlayerId): Result<Unit>
    
    /**
     * 设置成员职位
     */
    suspend fun setMemberRank(
        guildId: String,
        operatorId: PlayerId,
        targetId: PlayerId,
        newRank: GuildRank
    ): Result<Unit>
    
    /**
     * 转让会长
     */
    suspend fun transferLeadership(
        guildId: String,
        currentLeaderId: PlayerId,
        newLeaderId: PlayerId
    ): Result<Unit>
    
    /**
     * 增加公会经验
     */
    suspend fun addExperience(guildId: String, amount: Long): Result<Unit>
    
    /**
     * 增加公会资金
     */
    suspend fun addFunds(guildId: String, amount: Long, contributorId: PlayerId): Result<Unit>
    
    /**
     * 获取公会成员列表
     */
    suspend fun getMembers(guildId: String): List<GuildMember>
    
    /**
     * 获取公会排行榜
     */
    suspend fun getLeaderboard(limit: Int = 100): List<Guild>
}
