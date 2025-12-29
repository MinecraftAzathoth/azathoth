<template>
  <div class="container mx-auto px-4 py-8">
    <div class="max-w-4xl mx-auto">
      <h1 class="text-3xl font-bold text-center mb-8 text-gray-900 dark:text-white">
        {{ $t('generator.title') }}
      </h1>
      <p class="text-center text-gray-600 dark:text-gray-400 mb-12">
        {{ $t('generator.subtitle') }}
      </p>

      <!-- Steps -->
      <div class="flex justify-center mb-12">
        <div class="flex items-center">
          <template v-for="(step, index) in steps" :key="step.id">
            <div
              :class="[
                'flex items-center justify-center w-10 h-10 rounded-full font-semibold',
                currentStep >= index
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-200 dark:bg-gray-700 text-gray-500'
              ]"
            >
              {{ index + 1 }}
            </div>
            <div
              v-if="index < steps.length - 1"
              :class="[
                'w-20 h-1 mx-2',
                currentStep > index ? 'bg-primary-600' : 'bg-gray-200 dark:bg-gray-700'
              ]"
            ></div>
          </template>
        </div>
      </div>

      <!-- Step Content -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-8">
        <!-- Step 1: Template Selection -->
        <div v-if="currentStep === 0">
          <h2 class="text-xl font-semibold mb-6 text-gray-900 dark:text-white">
            {{ $t('generator.steps.template') }}
          </h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <button
              v-for="template in templates"
              :key="template.templateId"
              @click="selectTemplate(template)"
              :class="[
                'p-4 border-2 rounded-lg text-left transition-colors',
                selectedTemplate?.templateId === template.templateId
                  ? 'border-primary-600 bg-primary-50 dark:bg-primary-900/20'
                  : 'border-gray-200 dark:border-gray-700 hover:border-primary-400'
              ]"
            >
              <h3 class="font-semibold text-gray-900 dark:text-white mb-1">
                {{ template.name }}
              </h3>
              <p class="text-sm text-gray-600 dark:text-gray-400">
                {{ template.description }}
              </p>
            </button>
          </div>
        </div>

        <!-- Step 2: Project Configuration -->
        <div v-else-if="currentStep === 1">
          <h2 class="text-xl font-semibold mb-6 text-gray-900 dark:text-white">
            {{ $t('generator.steps.config') }}
          </h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('generator.fields.projectName') }} *
              </label>
              <input
                v-model="config.projectName"
                type="text"
                placeholder="my-awesome-plugin"
                class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('generator.fields.groupId') }} *
              </label>
              <input
                v-model="config.groupId"
                type="text"
                placeholder="com.example"
                class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('generator.fields.version') }}
              </label>
              <input
                v-model="config.version"
                type="text"
                placeholder="1.0.0"
                class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('generator.fields.author') }}
              </label>
              <input
                v-model="config.author"
                type="text"
                placeholder="Your Name"
                class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              />
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('generator.fields.description') }}
              </label>
              <textarea
                v-model="config.description"
                rows="2"
                placeholder="项目描述..."
                class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              ></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('generator.fields.kotlinVersion') }}
              </label>
              <select
                v-model="config.kotlinVersion"
                class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option v-for="v in kotlinVersions" :key="v" :value="v">{{ v }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                {{ $t('generator.fields.javaVersion') }}
              </label>
              <select
                v-model="config.javaVersion"
                class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              >
                <option v-for="v in javaVersions" :key="v" :value="v">{{ v }}</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Step 3: Feature Selection -->
        <div v-else-if="currentStep === 2">
          <h2 class="text-xl font-semibold mb-6 text-gray-900 dark:text-white">
            {{ $t('generator.steps.features') }}
          </h2>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <label
              v-for="feature in allFeatures"
              :key="feature"
              :class="[
                'flex items-center p-4 border-2 rounded-lg cursor-pointer transition-colors',
                config.features.includes(feature)
                  ? 'border-primary-600 bg-primary-50 dark:bg-primary-900/20'
                  : 'border-gray-200 dark:border-gray-700 hover:border-primary-400'
              ]"
            >
              <input
                type="checkbox"
                :value="feature"
                v-model="config.features"
                class="w-5 h-5 text-primary-600 rounded"
              />
              <span class="ml-3 text-gray-900 dark:text-white">
                {{ $t(`generator.features.${feature}`) }}
              </span>
            </label>
          </div>

          <div class="mt-6 flex items-center gap-6">
            <label class="flex items-center">
              <input
                type="checkbox"
                v-model="config.includeExamples"
                class="w-5 h-5 text-primary-600 rounded"
              />
              <span class="ml-2 text-gray-700 dark:text-gray-300">
                {{ $t('generator.fields.includeExamples') }}
              </span>
            </label>
            <label class="flex items-center">
              <input
                type="checkbox"
                v-model="config.includeTests"
                class="w-5 h-5 text-primary-600 rounded"
              />
              <span class="ml-2 text-gray-700 dark:text-gray-300">
                {{ $t('generator.fields.includeTests') }}
              </span>
            </label>
          </div>
        </div>

        <!-- Step 4: Generate -->
        <div v-else-if="currentStep === 3">
          <h2 class="text-xl font-semibold mb-6 text-gray-900 dark:text-white">
            {{ $t('generator.steps.generate') }}
          </h2>

          <div v-if="generating" class="text-center py-12">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
            <p class="text-gray-600 dark:text-gray-400">正在生成项目...</p>
          </div>

          <div v-else-if="downloadUrl" class="text-center py-12">
            <div class="w-16 h-16 bg-green-100 dark:bg-green-900 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <p class="text-lg text-gray-900 dark:text-white mb-6">项目生成成功！</p>
            <a
              :href="downloadUrl"
              class="inline-block px-8 py-3 bg-primary-600 text-white rounded-lg font-semibold hover:bg-primary-700 transition-colors"
            >
              {{ $t('generator.download') }}
            </a>
          </div>

          <div v-else>
            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4 mb-6">
              <h3 class="font-semibold text-gray-900 dark:text-white mb-2">配置预览</h3>
              <dl class="grid grid-cols-2 gap-2 text-sm">
                <dt class="text-gray-500">项目名称</dt>
                <dd class="text-gray-900 dark:text-white">{{ config.projectName }}</dd>
                <dt class="text-gray-500">Group ID</dt>
                <dd class="text-gray-900 dark:text-white">{{ config.groupId }}</dd>
                <dt class="text-gray-500">版本</dt>
                <dd class="text-gray-900 dark:text-white">{{ config.version }}</dd>
                <dt class="text-gray-500">功能模块</dt>
                <dd class="text-gray-900 dark:text-white">{{ config.features.length }} 个</dd>
              </dl>
            </div>
            <button
              @click="generateProject"
              class="w-full py-3 bg-primary-600 text-white rounded-lg font-semibold hover:bg-primary-700 transition-colors"
            >
              {{ $t('generator.generate') }}
            </button>
          </div>
        </div>

        <!-- Navigation -->
        <div class="flex justify-between mt-8 pt-6 border-t border-gray-200 dark:border-gray-700">
          <button
            v-if="currentStep > 0"
            @click="currentStep--"
            class="px-6 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
          >
            {{ $t('common.previous') }}
          </button>
          <div v-else></div>

          <button
            v-if="currentStep < 3"
            @click="nextStep"
            :disabled="!canProceed"
            class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ $t('common.next') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatureModule, ProjectType } from '~/types'
import type { TemplateInfo, ProjectConfig } from '~/types'

const { t } = useI18n()

const steps = [
  { id: 'template' },
  { id: 'config' },
  { id: 'features' },
  { id: 'generate' },
]

const currentStep = ref(0)
const selectedTemplate = ref<TemplateInfo | null>(null)
const generating = ref(false)
const downloadUrl = ref<string | null>(null)

// Mock data - in real app, fetch from API
const templates: TemplateInfo[] = [
  {
    templateId: 'game_plugin_basic',
    name: '游戏插件 - 基础模板',
    description: '适合入门开发者的基础游戏插件模板',
    projectType: ProjectType.GAME_PLUGIN,
    defaultFeatures: [FeatureModule.COMMAND_SYSTEM],
    preview: ''
  },
  {
    templateId: 'game_plugin_full',
    name: '游戏插件 - 完整模板',
    description: '包含所有游戏系统的完整模板',
    projectType: ProjectType.GAME_PLUGIN,
    defaultFeatures: [FeatureModule.SKILL_SYSTEM, FeatureModule.ITEM_SYSTEM, FeatureModule.QUEST_SYSTEM],
    preview: ''
  },
  {
    templateId: 'extension_plugin',
    name: '扩展服务插件',
    description: '用于开发扩展服务的模板',
    projectType: ProjectType.EXTENSION_PLUGIN,
    defaultFeatures: [FeatureModule.DATABASE, FeatureModule.GRPC_CLIENT],
    preview: ''
  },
]

const kotlinVersions = ['2.0.0', '1.9.24', '1.9.23']
const javaVersions = [21, 17, 11]
const allFeatures = Object.values(FeatureModule)

const config = reactive<ProjectConfig>({
  projectName: '',
  groupId: '',
  version: '1.0.0',
  description: '',
  author: '',
  projectType: ProjectType.GAME_PLUGIN,
  features: [],
  kotlinVersion: '2.0.0',
  javaVersion: 21,
  includeExamples: true,
  includeTests: true,
})

const canProceed = computed(() => {
  if (currentStep.value === 0) return !!selectedTemplate.value
  if (currentStep.value === 1) return config.projectName && config.groupId
  return true
})

const selectTemplate = (template: TemplateInfo) => {
  selectedTemplate.value = template
  config.projectType = template.projectType
  config.features = [...template.defaultFeatures]
}

const nextStep = () => {
  if (canProceed.value && currentStep.value < 3) {
    currentStep.value++
  }
}

const generateProject = async () => {
  generating.value = true
  try {
    // Mock API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    downloadUrl.value = '/api/generator/download/mock-token'
  } catch (error) {
    alert('生成失败')
  } finally {
    generating.value = false
  }
}

useHead({
  title: t('generator.title') + ' - Azathoth',
})
</script>
