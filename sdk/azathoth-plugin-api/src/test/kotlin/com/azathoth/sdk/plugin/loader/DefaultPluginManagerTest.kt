package com.azathoth.sdk.plugin.loader

import com.azathoth.sdk.plugin.AzathothPlugin
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class DefaultPluginManagerTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var mockLoader: PluginLoader
    private lateinit var manager: DefaultPluginManager

    private val testDescriptor = SimplePluginDescriptor(
        id = "test-plugin",
        name = "Test Plugin",
        version = "1.0.0",
        mainClass = "com.test.TestPlugin"
    )

    private lateinit var mockPlugin: AzathothPlugin
    private lateinit var loadedPlugin: SimpleLoadedPlugin

    @BeforeEach
    fun setup() {
        mockLoader = mockk(relaxed = true)
        mockPlugin = mockk(relaxed = true)
        every { mockPlugin.id } returns "test-plugin"
        every { mockPlugin.name } returns "Test Plugin"
        every { mockPlugin.version } returns "1.0.0"

        loadedPlugin = SimpleLoadedPlugin(
            descriptor = testDescriptor,
            instance = mockPlugin,
            state = PluginState.LOADED,
            filePath = tempDir.resolve("test-plugin.jar"),
            dataFolder = tempDir.resolve("test-plugin"),
            classLoader = this::class.java.classLoader
        )

        manager = DefaultPluginManager(tempDir, mockLoader)
    }

    @Test
    fun `loadAll discovers and loads plugins`() = runTest {
        coEvery { mockLoader.discover(tempDir) } returns listOf(testDescriptor)
        every { mockLoader.checkDependencies(testDescriptor) } returns SimpleDependencyCheckResult(satisfied = true)
        coEvery { mockLoader.load(testDescriptor) } returns loadedPlugin

        manager.loadAll()

        assertEquals(1, manager.getPlugins().size)
        assertTrue(manager.isPluginLoaded("test-plugin"))
        assertNotNull(manager.getPlugin("test-plugin"))
    }

    @Test
    fun `enablePlugin calls loader enable`() = runTest {
        // 先加载
        coEvery { mockLoader.discover(tempDir) } returns listOf(testDescriptor)
        every { mockLoader.checkDependencies(testDescriptor) } returns SimpleDependencyCheckResult(satisfied = true)
        coEvery { mockLoader.load(testDescriptor) } returns loadedPlugin
        coEvery { mockLoader.enable(any()) } answers {
            (firstArg<SimpleLoadedPlugin>()).state = PluginState.ENABLED
        }

        manager.loadAll()
        manager.enablePlugin("test-plugin")

        assertTrue(manager.isPluginEnabled("test-plugin"))
        coVerify { mockLoader.enable(any()) }
    }

    @Test
    fun `disablePlugin calls loader disable`() = runTest {
        coEvery { mockLoader.discover(tempDir) } returns listOf(testDescriptor)
        every { mockLoader.checkDependencies(testDescriptor) } returns SimpleDependencyCheckResult(satisfied = true)
        coEvery { mockLoader.load(testDescriptor) } returns loadedPlugin
        coEvery { mockLoader.enable(any()) } answers {
            (firstArg<SimpleLoadedPlugin>()).state = PluginState.ENABLED
        }
        coEvery { mockLoader.disable(any()) } answers {
            (firstArg<SimpleLoadedPlugin>()).state = PluginState.DISABLED
        }

        manager.loadAll()
        manager.enablePlugin("test-plugin")
        manager.disablePlugin("test-plugin")

        assertFalse(manager.isPluginEnabled("test-plugin"))
        coVerify { mockLoader.disable(any()) }
    }

    @Test
    fun `unloadPlugin removes plugin from manager`() = runTest {
        coEvery { mockLoader.discover(tempDir) } returns listOf(testDescriptor)
        every { mockLoader.checkDependencies(testDescriptor) } returns SimpleDependencyCheckResult(satisfied = true)
        coEvery { mockLoader.load(testDescriptor) } returns loadedPlugin
        coEvery { mockLoader.disable(any()) } answers {
            (firstArg<SimpleLoadedPlugin>()).state = PluginState.DISABLED
        }
        coEvery { mockLoader.unload(any()) } answers {
            (firstArg<SimpleLoadedPlugin>()).state = PluginState.UNLOADED
        }

        manager.loadAll()
        manager.unloadPlugin("test-plugin")

        assertNull(manager.getPlugin("test-plugin"))
        assertFalse(manager.isPluginLoaded("test-plugin"))
    }

    @Test
    fun `state listener is notified on state changes`() = runTest {
        val stateChanges = mutableListOf<Triple<String, PluginState, PluginState>>()
        val listener = object : PluginStateListener {
            override suspend fun onStateChange(plugin: LoadedPlugin, oldState: PluginState, newState: PluginState) {
                stateChanges.add(Triple(plugin.descriptor.id, oldState, newState))
            }
        }

        coEvery { mockLoader.discover(tempDir) } returns listOf(testDescriptor)
        every { mockLoader.checkDependencies(testDescriptor) } returns SimpleDependencyCheckResult(satisfied = true)
        coEvery { mockLoader.load(testDescriptor) } returns loadedPlugin
        coEvery { mockLoader.enable(any()) } answers {
            (firstArg<SimpleLoadedPlugin>()).state = PluginState.ENABLED
        }

        manager.addStateListener(listener)
        manager.loadAll()
        manager.enablePlugin("test-plugin")

        assertEquals(2, stateChanges.size)
        assertEquals(PluginState.LOADED, stateChanges[0].third)
        assertEquals(PluginState.ENABLED, stateChanges[1].third)
    }

    @Test
    fun `loadAll skips plugins with unsatisfied dependencies`() = runTest {
        coEvery { mockLoader.discover(tempDir) } returns listOf(testDescriptor)
        every { mockLoader.checkDependencies(testDescriptor) } returns SimpleDependencyCheckResult(
            satisfied = false,
            missingDependencies = listOf(SimplePluginDependency("missing-dep"))
        )

        manager.loadAll()

        assertEquals(0, manager.getPlugins().size)
    }

    @Test
    fun `enableAll and disableAll work correctly`() = runTest {
        coEvery { mockLoader.discover(tempDir) } returns listOf(testDescriptor)
        every { mockLoader.checkDependencies(testDescriptor) } returns SimpleDependencyCheckResult(satisfied = true)
        coEvery { mockLoader.load(testDescriptor) } returns loadedPlugin
        coEvery { mockLoader.enable(any()) } answers {
            (firstArg<SimpleLoadedPlugin>()).state = PluginState.ENABLED
        }
        coEvery { mockLoader.disable(any()) } answers {
            (firstArg<SimpleLoadedPlugin>()).state = PluginState.DISABLED
        }

        manager.loadAll()
        manager.enableAll()
        assertTrue(manager.isPluginEnabled("test-plugin"))

        manager.disableAll()
        assertFalse(manager.isPluginEnabled("test-plugin"))
    }
}
