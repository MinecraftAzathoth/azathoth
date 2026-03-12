package com.azathoth.services.guild.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

// region Data Classes

data class SimpleGuildLevel(
    override val level: Int,
    override val maxMembers: Int,
    override val experienceRequired: Long,
    override val unlockedFeatures: Set<String>
) : GuildLevel

data class SimpleGuild(
    override val guildId: String,
    override var name: String,
    override var tag: String,
    override var announcement: String = "",
    override var description: String = "",
    override val level: Int = 1,
    override val experience: Long = 0,
    override val leaderId: PlayerId,
    override val memberCount: Int = 1,
    override val maxMembers: Int = 50,
    override var funds: Long = 0,
    override val createdAt: Long = System.currentTimeMillis(),
    override var recruiting: Boolean = true,
    override var requiredLevel: Int = 0,
    override var requireApplication: Boolean = false
) : Guild

data class SimpleGuildMember(
    override val playerId: PlayerId,
    override val guildId: String,
    override var rank: GuildRank,
    override var contribution: Long = 0,
    override var weeklyContribution: Long = 0,
    override val joinedAt: Long = System.currentTimeMillis(),
    override var lastActiveAt: Long = System.currentTimeMillis(),
    override var note: String? = null
) : GuildMember

data class SimpleGuildApplication(
    override val applicationId: String,
    override val playerId: PlayerId,
    override val guildId: String,
    override val message: String,
    override val appliedAt: Long = System.currentTimeMillis(),
    override val status: ApplicationStatus = ApplicationStatus.PENDING
) : GuildApplication

// endregion

private val GUILD_LEVELS = listOf(
    SimpleGuildLevel(1, 50, 0, emptySet()),
    SimpleGuildLevel(2, 80, 10000, setOf("guild_shop")),
    SimpleGuildLevel(3, 100, 30000, setOf("guild_shop", "guild_warehouse")),
    SimpleGuildLevel(4, 150, 80000, setOf("guild_shop", "guild_warehouse", "guild_boss")),
    SimpleGuildLevel(5, 200, 200000, setOf("guild_shop", "guild_warehouse", "guild_boss", "guild_territory"))
)

class DefaultGuildService : GuildService {

    private val guilds = ConcurrentHashMap<String, SimpleGuild>()
    private val members = ConcurrentHashMap<String, MutableList<SimpleGuildMember>>() // guildId -> members
    private val playerGuild = ConcurrentHashMap<PlayerId, String>() // playerId -> guildId
    private val applications = ConcurrentHashMap<String, SimpleGuildApplication>()

    override suspend fun createGuild(leaderId: PlayerId, name: String, tag: String): Result<Guild> {
        if (playerGuild.containsKey(leaderId)) {
            return Result.failure(ErrorCodes.ALREADY_EXISTS, "玩家已在公会中")
        }
        if (name.isBlank() || name.length > 20) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "公会名称无效")
        }
        if (tag.isBlank() || tag.length > 5) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "公会标签无效")
        }
        if (guilds.values.any { it.name == name }) {
            return Result.failure(ErrorCodes.ALREADY_EXISTS, "公会名称已存在")
        }

        val guildId = UUID.randomUUID().toString()
        val guild = SimpleGuild(guildId = guildId, name = name, tag = tag, leaderId = leaderId)
        val member = SimpleGuildMember(playerId = leaderId, guildId = guildId, rank = GuildRank.LEADER)

        guilds[guildId] = guild
        members[guildId] = mutableListOf(member)
        playerGuild[leaderId] = guildId

        logger.info { "公会创建成功: $name [$tag] by $leaderId" }
        return Result.success(guild)
    }

    override suspend fun disbandGuild(guildId: String, operatorId: PlayerId): Result<Unit> {
        val guild = guilds[guildId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "公会不存在")
        if (guild.leaderId != operatorId) {
            return Result.failure(ErrorCodes.PERMISSION_DENIED, "只有会长可以解散公会")
        }

        members[guildId]?.forEach { playerGuild.remove(it.playerId) }
        members.remove(guildId)
        guilds.remove(guildId)
        applications.values.removeIf { it.guildId == guildId }

        logger.info { "公会已解散: ${guild.name}" }
        return Result.success(Unit)
    }

    override suspend fun getGuild(guildId: String): Guild? = guilds[guildId]?.withMemberCount()

    override suspend fun getPlayerGuild(playerId: PlayerId): Guild? {
        val guildId = playerGuild[playerId] ?: return null
        return guilds[guildId]?.withMemberCount()
    }

    override suspend fun searchGuilds(keyword: String, limit: Int): List<Guild> {
        return guilds.values
            .filter { it.name.contains(keyword, ignoreCase = true) || it.tag.contains(keyword, ignoreCase = true) }
            .take(limit)
            .map { it.withMemberCount() }
    }

    override suspend fun applyToGuild(playerId: PlayerId, guildId: String, message: String): Result<GuildApplication> {
        if (playerGuild.containsKey(playerId)) {
            return Result.failure(ErrorCodes.ALREADY_EXISTS, "玩家已在公会中")
        }
        val guild = guilds[guildId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "公会不存在")
        if (!guild.recruiting) {
            return Result.failure(ErrorCodes.PERMISSION_DENIED, "公会未开放招募")
        }
        if (applications.values.any { it.playerId == playerId && it.guildId == guildId && it.status == ApplicationStatus.PENDING }) {
            return Result.failure(ErrorCodes.ALREADY_EXISTS, "已有待处理的申请")
        }

        if (!guild.requireApplication) {
            // 直接加入
            addMemberToGuild(playerId, guildId)
            val app = SimpleGuildApplication(
                applicationId = UUID.randomUUID().toString(),
                playerId = playerId, guildId = guildId, message = message,
                status = ApplicationStatus.APPROVED
            )
            return Result.success(app)
        }

        val app = SimpleGuildApplication(
            applicationId = UUID.randomUUID().toString(),
            playerId = playerId, guildId = guildId, message = message
        )
        applications[app.applicationId] = app
        return Result.success(app)
    }

    override suspend fun handleApplication(applicationId: String, approved: Boolean, operatorId: PlayerId): Result<Unit> {
        val app = applications[applicationId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "申请不存在")
        if (app.status != ApplicationStatus.PENDING) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "申请已处理")
        }

        val operatorMember = members[app.guildId]?.find { it.playerId == operatorId }
            ?: return Result.failure(ErrorCodes.PERMISSION_DENIED, "操作者不是公会成员")
        if (operatorMember.rank.priority < GuildRank.ELDER.priority) {
            return Result.failure(ErrorCodes.PERMISSION_DENIED, "权限不足")
        }

        val newStatus = if (approved) ApplicationStatus.APPROVED else ApplicationStatus.REJECTED
        applications[applicationId] = app.copy(status = newStatus)

        if (approved) {
            val guild = guilds[app.guildId]!!
            val currentCount = members[app.guildId]?.size ?: 0
            if (currentCount >= guild.maxMembers) {
                applications[applicationId] = app.copy(status = ApplicationStatus.REJECTED)
                return Result.failure(ErrorCodes.INVALID_ARGUMENT, "公会成员已满")
            }
            addMemberToGuild(app.playerId, app.guildId)
        }

        return Result.success(Unit)
    }

    override suspend fun invitePlayer(guildId: String, inviterId: PlayerId, inviteeId: PlayerId): Result<Unit> {
        guilds[guildId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "公会不存在")
        val inviter = members[guildId]?.find { it.playerId == inviterId }
            ?: return Result.failure(ErrorCodes.PERMISSION_DENIED, "邀请者不是公会成员")
        if (inviter.rank.priority < GuildRank.ELDER.priority) {
            return Result.failure(ErrorCodes.PERMISSION_DENIED, "权限不足")
        }
        if (playerGuild.containsKey(inviteeId)) {
            return Result.failure(ErrorCodes.ALREADY_EXISTS, "目标玩家已在公会中")
        }

        addMemberToGuild(inviteeId, guildId)
        return Result.success(Unit)
    }

    override suspend fun kickMember(guildId: String, operatorId: PlayerId, targetId: PlayerId, reason: String): Result<Unit> {
        guilds[guildId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "公会不存在")
        val operator = members[guildId]?.find { it.playerId == operatorId }
            ?: return Result.failure(ErrorCodes.PERMISSION_DENIED, "操作者不是公会成员")
        val target = members[guildId]?.find { it.playerId == targetId }
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "目标不是公会成员")
        if (operator.rank.priority <= target.rank.priority) {
            return Result.failure(ErrorCodes.PERMISSION_DENIED, "权限不足，无法踢出同级或更高职位的成员")
        }

        members[guildId]?.removeIf { it.playerId == targetId }
        playerGuild.remove(targetId)
        logger.info { "成员 $targetId 被踢出公会 $guildId" }
        return Result.success(Unit)
    }

    override suspend fun leaveGuild(playerId: PlayerId): Result<Unit> {
        val guildId = playerGuild[playerId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "玩家不在公会中")
        val guild = guilds[guildId]!!
        if (guild.leaderId == playerId) {
            return Result.failure(ErrorCodes.PERMISSION_DENIED, "会长不能直接离开公会，请先转让会长或解散公会")
        }

        members[guildId]?.removeIf { it.playerId == playerId }
        playerGuild.remove(playerId)
        return Result.success(Unit)
    }

    override suspend fun setMemberRank(guildId: String, operatorId: PlayerId, targetId: PlayerId, newRank: GuildRank): Result<Unit> {
        guilds[guildId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "公会不存在")
        val operator = members[guildId]?.find { it.playerId == operatorId }
            ?: return Result.failure(ErrorCodes.PERMISSION_DENIED, "操作者不是公会成员")
        val target = members[guildId]?.find { it.playerId == targetId }
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "目标不是公会成员")

        if (newRank == GuildRank.LEADER) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "不能通过此方法设置会长，请使用转让会长功能")
        }
        if (operator.rank.priority <= target.rank.priority && operator.rank != GuildRank.LEADER) {
            return Result.failure(ErrorCodes.PERMISSION_DENIED, "权限不足")
        }
        if (newRank.priority >= operator.rank.priority && operator.rank != GuildRank.LEADER) {
            return Result.failure(ErrorCodes.PERMISSION_DENIED, "不能设置与自己同级或更高的职位")
        }

        target.rank = newRank
        return Result.success(Unit)
    }

    override suspend fun transferLeadership(guildId: String, currentLeaderId: PlayerId, newLeaderId: PlayerId): Result<Unit> {
        val guild = guilds[guildId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "公会不存在")
        if (guild.leaderId != currentLeaderId) {
            return Result.failure(ErrorCodes.PERMISSION_DENIED, "只有会长可以转让会长")
        }
        val newLeader = members[guildId]?.find { it.playerId == newLeaderId }
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "目标不是公会成员")
        val currentLeader = members[guildId]?.find { it.playerId == currentLeaderId }!!

        currentLeader.rank = GuildRank.CO_LEADER
        newLeader.rank = GuildRank.LEADER
        guilds[guildId] = guild.copy(leaderId = newLeaderId)

        logger.info { "公会 $guildId 会长从 $currentLeaderId 转让给 $newLeaderId" }
        return Result.success(Unit)
    }

    override suspend fun addExperience(guildId: String, amount: Long): Result<Unit> {
        val guild = guilds[guildId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "公会不存在")
        if (amount <= 0) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "经验值必须为正数")

        val newExp = guild.experience + amount
        var newLevel = guild.level
        var newMaxMembers = guild.maxMembers
        for (gl in GUILD_LEVELS) {
            if (gl.level > newLevel && newExp >= gl.experienceRequired) {
                newLevel = gl.level
                newMaxMembers = gl.maxMembers
            }
        }

        guilds[guildId] = guild.copy(experience = newExp, level = newLevel, maxMembers = newMaxMembers)
        return Result.success(Unit)
    }

    override suspend fun addFunds(guildId: String, amount: Long, contributorId: PlayerId): Result<Unit> {
        val guild = guilds[guildId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "公会不存在")
        if (amount <= 0) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "资金必须为正数")

        val member = members[guildId]?.find { it.playerId == contributorId }
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "贡献者不是公会成员")

        guilds[guildId] = guild.copy(funds = guild.funds + amount)
        member.contribution += amount
        member.weeklyContribution += amount
        return Result.success(Unit)
    }

    override suspend fun getMembers(guildId: String): List<GuildMember> {
        return members[guildId]?.toList() ?: emptyList()
    }

    override suspend fun getLeaderboard(limit: Int): List<Guild> {
        return guilds.values
            .sortedByDescending { it.experience }
            .take(limit)
            .map { it.withMemberCount() }
    }

    // region Helpers

    private fun addMemberToGuild(playerId: PlayerId, guildId: String) {
        val member = SimpleGuildMember(playerId = playerId, guildId = guildId, rank = GuildRank.NEWCOMER)
        members.getOrPut(guildId) { mutableListOf() }.add(member)
        playerGuild[playerId] = guildId
    }

    private fun SimpleGuild.withMemberCount(): SimpleGuild {
        return copy(memberCount = members[guildId]?.size ?: 0)
    }

    // endregion
}
