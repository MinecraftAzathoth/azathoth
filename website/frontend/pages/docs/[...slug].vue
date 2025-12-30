<template>
  <div class="prose prose-gray dark:prose-invert max-w-none">
    <ContentDoc :path="contentPath" v-slot="{ doc }">
      <template v-if="doc">
        <h1>{{ doc.title }}</h1>
        <ContentRenderer :value="doc" />

        <!-- Navigation -->
        <div class="flex justify-between items-center mt-12 pt-8 border-t border-gray-200 dark:border-gray-700 not-prose">
          <NuxtLink
            v-if="doc.prev"
            :to="doc.prev._path"
            class="flex items-center text-gray-600 dark:text-gray-400 hover:text-primary-600"
          >
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            <span>{{ doc.prev.title }}</span>
          </NuxtLink>
          <div v-else></div>

          <NuxtLink
            v-if="doc.next"
            :to="doc.next._path"
            class="flex items-center text-gray-600 dark:text-gray-400 hover:text-primary-600"
          >
            <span>{{ doc.next.title }}</span>
            <svg class="w-5 h-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </NuxtLink>
          <div v-else></div>
        </div>
      </template>
    </ContentDoc>

    <!-- Fallback content when @nuxt/content is not available -->
    <template v-if="!hasNuxtContent">
      <component :is="currentPage" v-if="currentPage" />
      <div v-else class="text-center py-12">
        <p class="text-gray-500 dark:text-gray-400">文档内容正在建设中...</p>
        <NuxtLink to="/docs" class="text-primary-600 hover:underline mt-4 inline-block">
          返回文档首页
        </NuxtLink>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()

definePageMeta({
  layout: 'docs',
})

// Check if @nuxt/content is available
const hasNuxtContent = ref(false)

// Build content path from route
const contentPath = computed(() => {
  const slug = route.params.slug
  if (Array.isArray(slug)) {
    return `/docs/${slug.join('/')}`
  }
  return `/docs/${slug || ''}`
})

// Inline page content mapping (fallback when @nuxt/content is not configured)
const pageContent = computed(() => {
  const slug = route.params.slug
  const path = Array.isArray(slug) ? slug.join('/') : slug

  // Map paths to content
  const contentMap: Record<string, any> = {
    'getting-started': GettingStartedContent,
    'installation': InstallationContent,
    'project-structure': ProjectStructureContent,
    'core/architecture': ArchitectureContent,
    'core/lifecycle': LifecycleContent,
    'core/dependency-injection': DependencyInjectionContent,
    'core/configuration': ConfigurationContent,
    'core/events': EventsContent,
    'systems/skill': SkillSystemContent,
    'systems/dungeon': DungeonSystemContent,
    'systems/quest': QuestSystemContent,
  }

  return contentMap[path] || null
})

const currentPage = computed(() => pageContent.value)

// Dynamic page title
const pageTitle = computed(() => {
  const slug = route.params.slug
  const path = Array.isArray(slug) ? slug[slug.length - 1] : slug
  const titles: Record<string, string> = {
    'getting-started': '快速开始',
    'installation': '安装指南',
    'project-structure': '项目结构',
    'architecture': '架构设计',
    'lifecycle': '生命周期',
    'dependency-injection': '依赖注入',
    'configuration': '配置系统',
    'events': '事件系统',
    'skill': '技能系统',
    'dungeon': '副本系统',
    'quest': '任务系统',
  }
  return titles[path || ''] || '文档'
})

useHead({
  title: () => `${pageTitle.value} - Azathoth 文档`,
})

// Try to detect @nuxt/content
onMounted(() => {
  // Check if ContentDoc component is available
  hasNuxtContent.value = !!resolveComponent('ContentDoc')
})
</script>

<script lang="ts">
// Inline content components (fallback)
const GettingStartedContent = defineComponent({
  template: `
    <div>
      <h1>快速开始</h1>
      <p>本指南将帮助您快速创建第一个 Azathoth 项目。</p>

      <h2>前置要求</h2>
      <ul>
        <li>JDK 21 或更高版本</li>
        <li>Gradle 8.0 或更高版本</li>
        <li>Minecraft 服务端 (Paper/Spigot 1.20+)</li>
      </ul>

      <h2>创建项目</h2>
      <p>使用 Azathoth 项目生成器创建新项目：</p>
      <pre><code>npx create-azathoth-app my-plugin</code></pre>

      <p>或者手动配置 Gradle：</p>
      <pre><code>plugins {
    id("com.azathoth.gradle") version "1.0.0"
}

azathoth {
    pluginId = "my-plugin"
    pluginName = "My Plugin"
    version = "1.0.0"
}</code></pre>

      <h2>项目结构</h2>
      <pre><code>my-plugin/
├── build.gradle.kts
├── settings.gradle.kts
└── src/
    └── main/
        └── kotlin/
            └── com/example/
                └── MyPlugin.kt</code></pre>

      <h2>编写插件</h2>
      <pre><code>@AzathothPlugin(
    id = "my-plugin",
    name = "My Plugin",
    version = "1.0.0"
)
class MyPlugin : AzathothPluginBase() {
    override fun onEnable() {
        logger.info { "Hello from Azathoth!" }
    }
}</code></pre>

      <h2>构建和运行</h2>
      <pre><code>./gradlew build</code></pre>
      <p>将生成的 JAR 文件复制到服务器的 plugins 目录即可。</p>
    </div>
  `,
})

const InstallationContent = defineComponent({
  template: `
    <div>
      <h1>安装指南</h1>
      <p>详细的安装和环境配置指南。</p>

      <h2>系统要求</h2>
      <ul>
        <li>操作系统：Windows 10+, macOS 10.15+, Linux</li>
        <li>内存：建议 8GB 以上</li>
        <li>磁盘空间：至少 2GB 可用空间</li>
      </ul>

      <h2>安装 JDK</h2>
      <p>推荐使用 Eclipse Temurin JDK 21：</p>
      <pre><code># macOS (Homebrew)
brew install temurin21

# Windows (Scoop)
scoop install temurin21-jdk

# Linux (SDKMAN)
sdk install java 21-tem</code></pre>

      <h2>安装 Gradle</h2>
      <p>推荐使用 Gradle Wrapper，无需手动安装。</p>

      <h2>IDE 配置</h2>
      <p>推荐使用 IntelliJ IDEA，安装以下插件：</p>
      <ul>
        <li>Kotlin</li>
        <li>Minecraft Development</li>
      </ul>
    </div>
  `,
})

const ProjectStructureContent = defineComponent({
  template: `
    <div>
      <h1>项目结构</h1>
      <p>了解 Azathoth 项目的标准目录结构。</p>

      <h2>标准结构</h2>
      <pre><code>my-plugin/
├── build.gradle.kts          # 构建配置
├── settings.gradle.kts       # 项目设置
├── gradle.properties         # Gradle 属性
├── src/
│   ├── main/
│   │   ├── kotlin/           # Kotlin 源代码
│   │   │   └── com/example/
│   │   │       ├── MyPlugin.kt
│   │   │       ├── commands/     # 命令
│   │   │       ├── listeners/    # 事件监听器
│   │   │       ├── services/     # 服务
│   │   │       └── skills/       # 技能
│   │   └── resources/
│   │       ├── plugin.yml        # 插件描述
│   │       └── config.yml        # 默认配置
│   └── test/
│       └── kotlin/               # 测试代码
└── libs/                         # 本地依赖</code></pre>

      <h2>模块化项目</h2>
      <p>对于大型项目，推荐使用多模块结构。</p>
    </div>
  `,
})

const ArchitectureContent = defineComponent({
  template: `
    <div>
      <h1>架构设计</h1>
      <p>Azathoth 采用模块化、可扩展的架构设计。</p>

      <h2>核心组件</h2>
      <ul>
        <li><strong>Core</strong>: 核心运行时，管理插件生命周期</li>
        <li><strong>DI Container</strong>: 基于 Koin 的依赖注入容器</li>
        <li><strong>Event Bus</strong>: 事件发布订阅系统</li>
        <li><strong>Config</strong>: 配置管理系统</li>
      </ul>

      <h2>模块系统</h2>
      <p>Azathoth 的模块可以独立加载和卸载，支持热更新。</p>

      <h2>服务层</h2>
      <p>所有业务逻辑通过服务接口暴露，便于测试和替换实现。</p>
    </div>
  `,
})

const LifecycleContent = defineComponent({
  template: `
    <div>
      <h1>生命周期</h1>
      <p>理解 Azathoth 插件的生命周期管理。</p>

      <h2>生命周期阶段</h2>
      <ol>
        <li><strong>LOADING</strong>: 加载插件类和资源</li>
        <li><strong>INITIALIZING</strong>: 初始化依赖注入</li>
        <li><strong>ENABLING</strong>: 启用插件功能</li>
        <li><strong>RUNNING</strong>: 正常运行状态</li>
        <li><strong>DISABLING</strong>: 禁用插件功能</li>
        <li><strong>UNLOADING</strong>: 卸载插件</li>
      </ol>

      <h2>生命周期回调</h2>
      <pre><code>class MyPlugin : AzathothPluginBase() {
    override fun onLoad() {
        // 加载阶段
    }

    override fun onEnable() {
        // 启用阶段
    }

    override fun onDisable() {
        // 禁用阶段
    }
}</code></pre>
    </div>
  `,
})

const DependencyInjectionContent = defineComponent({
  template: `
    <div>
      <h1>依赖注入</h1>
      <p>Azathoth 使用 Koin 作为依赖注入框架。</p>

      <h2>基本用法</h2>
      <pre><code>class MyService {
    fun doSomething() = "Hello"
}

class MyPlugin : AzathothPluginBase() {
    @Inject
    lateinit var myService: MyService

    override fun configureModules() = module {
        single { MyService() }
    }
}</code></pre>

      <h2>作用域</h2>
      <ul>
        <li><strong>single</strong>: 单例，整个应用共享</li>
        <li><strong>factory</strong>: 每次请求创建新实例</li>
        <li><strong>scoped</strong>: 在特定作用域内共享</li>
      </ul>
    </div>
  `,
})

const ConfigurationContent = defineComponent({
  template: `
    <div>
      <h1>配置系统</h1>
      <p>灵活的配置管理系统。</p>

      <h2>配置文件</h2>
      <p>支持 YAML、JSON、HOCON 格式。</p>

      <h2>类型安全配置</h2>
      <pre><code>@ConfigSection("database")
data class DatabaseConfig(
    val host: String = "localhost",
    val port: Int = 5432,
    val username: String = "root",
    val password: String = ""
)

class MyPlugin : AzathothPluginBase() {
    @Config
    lateinit var dbConfig: DatabaseConfig
}</code></pre>
    </div>
  `,
})

const EventsContent = defineComponent({
  template: `
    <div>
      <h1>事件系统</h1>
      <p>基于发布订阅模式的事件系统。</p>

      <h2>监听事件</h2>
      <pre><code>class MyListener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.sendMessage("Welcome!")
    }
}</code></pre>

      <h2>自定义事件</h2>
      <pre><code>class MyCustomEvent(
    val data: String
) : AzathothEvent()

// 发布事件
eventBus.publish(MyCustomEvent("hello"))</code></pre>
    </div>
  `,
})

const SkillSystemContent = defineComponent({
  template: `
    <div>
      <h1>技能系统</h1>
      <p>强大的技能系统，支持复杂的技能效果。</p>

      <h2>创建技能</h2>
      <pre><code>@Skill(
    id = "fireball",
    name = "火球术",
    cooldown = 5.0,
    manaCost = 20
)
class FireballSkill : ActiveSkill() {
    override fun cast(caster: Player, target: Location) {
        val fireball = caster.world.spawnEntity(
            caster.eyeLocation,
            EntityType.FIREBALL
        ) as Fireball
        fireball.direction = caster.location.direction
    }
}</code></pre>

      <h2>技能类型</h2>
      <ul>
        <li><strong>ActiveSkill</strong>: 主动释放技能</li>
        <li><strong>PassiveSkill</strong>: 被动技能</li>
        <li><strong>ToggleSkill</strong>: 切换类技能</li>
        <li><strong>ChannelSkill</strong>: 引导类技能</li>
      </ul>
    </div>
  `,
})

const DungeonSystemContent = defineComponent({
  template: `
    <div>
      <h1>副本系统</h1>
      <p>完整的副本实例化和管理系统。</p>

      <h2>创建副本</h2>
      <pre><code>@Dungeon(
    id = "dark-cave",
    name = "黑暗洞穴",
    minPlayers = 1,
    maxPlayers = 5,
    timeLimit = 1800
)
class DarkCaveDungeon : DungeonBase() {
    override fun onStart(instance: DungeonInstance) {
        // 副本开始逻辑
    }

    override fun onComplete(instance: DungeonInstance) {
        // 副本完成逻辑
    }
}</code></pre>
    </div>
  `,
})

const QuestSystemContent = defineComponent({
  template: `
    <div>
      <h1>任务系统</h1>
      <p>灵活的任务定义和追踪系统。</p>

      <h2>定义任务</h2>
      <pre><code>@Quest(
    id = "first-blood",
    name = "初次猎杀",
    description = "击杀 10 只僵尸"
)
class FirstBloodQuest : QuestBase() {
    override fun createObjectives() = listOf(
        KillObjective(EntityType.ZOMBIE, 10)
    )

    override fun getRewards() = listOf(
        ExpReward(100),
        ItemReward(Material.DIAMOND, 5)
    )
}</code></pre>
    </div>
  `,
})
</script>

<style>
.prose pre {
  @apply bg-gray-900 text-gray-300 rounded-lg;
}

.prose code {
  @apply text-primary-600 dark:text-primary-400;
}

.prose pre code {
  @apply text-gray-300;
}
</style>
