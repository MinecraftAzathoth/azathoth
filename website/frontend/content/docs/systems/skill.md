---
title: '技能系统'
description: '创建强大的技能和法术效果'
navigation:
  order: 1
---

# 技能系统

Azathoth 提供了功能强大的技能系统，支持创建各种类型的技能效果。

## 技能类型

### ActiveSkill - 主动技能

需要玩家主动释放的技能：

```kotlin
@Skill(
    id = "fireball",
    name = "火球术",
    description = "向目标发射一枚火球",
    cooldown = 5.0,
    manaCost = 30,
    castTime = 0.5
)
class FireballSkill : ActiveSkill() {

    override fun cast(caster: Player, context: SkillContext) {
        val target = context.targetLocation ?: return

        // 创建火球投射物
        val fireball = caster.world.spawnEntity(
            caster.eyeLocation,
            EntityType.FIREBALL
        ) as Fireball

        // 设置方向和速度
        fireball.direction = target.subtract(caster.eyeLocation).toVector().normalize()
        fireball.velocity = fireball.direction.multiply(2.0)

        // 设置伤害
        fireball.yield = 0f // 禁用爆炸
    }

    override fun onHit(event: SkillHitEvent) {
        // 造成魔法伤害
        event.target.damage(50.0, DamageType.MAGIC)

        // 点燃效果
        event.target.fireTicks = 100
    }
}
```

### PassiveSkill - 被动技能

持续生效的被动效果：

```kotlin
@Skill(
    id = "thick_skin",
    name = "厚皮",
    description = "减少受到的物理伤害"
)
class ThickSkinSkill : PassiveSkill() {

    @Subscribe
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        if (!hasSkill(event.entity as Player)) return

        // 减少 20% 物理伤害
        if (event.cause == DamageCause.ENTITY_ATTACK) {
            event.damage *= 0.8
        }
    }
}
```

### ToggleSkill - 切换技能

可以开关的持续效果技能：

```kotlin
@Skill(
    id = "stealth",
    name = "潜行",
    description = "进入隐身状态",
    manaCostPerSecond = 5
)
class StealthSkill : ToggleSkill() {

    override fun onActivate(player: Player) {
        // 隐身效果
        player.addPotionEffect(
            PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 0, false, false)
        )

        // 禁用碰撞
        player.setCollidable(false)
    }

    override fun onDeactivate(player: Player) {
        player.removePotionEffect(PotionEffectType.INVISIBILITY)
        player.setCollidable(true)
    }

    override fun canMaintain(player: Player): Boolean {
        // 攻击时解除隐身
        return !player.hasMetadata("recently_attacked")
    }
}
```

### ChannelSkill - 引导技能

需要持续引导的技能：

```kotlin
@Skill(
    id = "healing_beam",
    name = "治疗光束",
    description = "持续治疗目标",
    channelTime = 5.0,
    tickInterval = 0.5
)
class HealingBeamSkill : ChannelSkill() {

    override fun onChannelStart(caster: Player, context: SkillContext) {
        // 播放开始动画
        caster.world.spawnParticle(
            Particle.HEART,
            caster.location.add(0.0, 2.0, 0.0),
            10
        )
    }

    override fun onChannelTick(caster: Player, context: SkillContext, tick: Int) {
        val target = context.targetEntity as? LivingEntity ?: return

        // 每次治疗
        target.health = (target.health + 2.0).coerceAtMost(target.maxHealth)

        // 光束粒子效果
        drawBeam(caster.eyeLocation, target.location.add(0.0, 1.0, 0.0))
    }

    override fun onChannelComplete(caster: Player, context: SkillContext) {
        // 完成时额外治疗
        val target = context.targetEntity as? LivingEntity ?: return
        target.health = target.maxHealth
    }

    override fun onChannelInterrupt(caster: Player, context: SkillContext, reason: InterruptReason) {
        caster.sendMessage("§c引导被打断!")
    }
}
```

## 技能效果

### 伤害效果

```kotlin
class DamageEffect(
    val amount: Double,
    val type: DamageType,
    val scaling: StatScaling? = null
) : SkillEffect {

    override fun apply(caster: Player, target: LivingEntity, context: EffectContext) {
        var damage = amount

        // 属性缩放
        scaling?.let {
            damage += caster.getAttribute(it.stat)?.value ?: 0.0 * it.ratio
        }

        // 应用伤害
        target.damage(damage, type, caster)
    }
}
```

### 状态效果

```kotlin
class StatusEffect(
    val type: StatusType,
    val duration: Duration,
    val stacks: Int = 1
) : SkillEffect {

    override fun apply(caster: Player, target: LivingEntity, context: EffectContext) {
        val statusManager = getService<StatusManager>()
        statusManager.applyStatus(target, type, duration, stacks)
    }
}
```

### 区域效果

```kotlin
class AreaEffect(
    val radius: Double,
    val effects: List<SkillEffect>,
    val targetType: TargetType = TargetType.ENEMIES
) : SkillEffect {

    override fun apply(caster: Player, location: Location, context: EffectContext) {
        val targets = location.world.getNearbyLivingEntities(location, radius)
            .filter { matchesTargetType(caster, it, targetType) }

        for (target in targets) {
            for (effect in effects) {
                effect.apply(caster, target, context)
            }
        }
    }
}
```

## 技能树

支持技能升级和天赋系统：

```kotlin
@SkillTree(
    id = "fire_mage",
    name = "火焰法师"
)
class FireMageTree : SkillTreeBase() {

    override fun defineNodes(): List<SkillNode> = listOf(
        SkillNode(
            skill = FireballSkill::class,
            position = Position(0, 0),
            maxLevel = 5,
            requirements = emptyList()
        ),
        SkillNode(
            skill = MeteorSkill::class,
            position = Position(0, 1),
            maxLevel = 3,
            requirements = listOf(
                SkillRequirement(FireballSkill::class, 3)
            )
        ),
        SkillNode(
            skill = InfernoSkill::class,
            position = Position(0, 2),
            maxLevel = 1,
            requirements = listOf(
                SkillRequirement(MeteorSkill::class, 3),
                LevelRequirement(50)
            )
        )
    )
}
```

## 技能配置

技能可以通过配置文件调整参数：

```yaml
# skills/fireball.yml
fireball:
  cooldown: 5.0
  mana_cost: 30
  damage:
    base: 50
    scaling:
      stat: MAGIC_POWER
      ratio: 0.8
  effects:
    burn:
      duration: 5s
      damage_per_tick: 5
```

## 下一步

- [副本系统](/docs/systems/dungeon) - 创建副本实例
- [任务系统](/docs/systems/quest) - 定义任务目标
- [物品系统](/docs/systems/item) - 自定义物品属性
