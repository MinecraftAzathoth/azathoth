<template>
  <div class="container mx-auto px-4 py-8">
    <div class="flex gap-8">
      <!-- Sidebar -->
      <aside class="hidden lg:block w-64 flex-shrink-0">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 sticky top-24">
          <h2 class="font-semibold text-gray-900 dark:text-white mb-4">
            {{ $t('forum.categories') }}
          </h2>
          <nav class="space-y-1">
            <NuxtLink
              to="/forum"
              :class="[
                'block px-3 py-2 rounded-lg transition-colors',
                !route.query.category
                  ? 'bg-primary-50 dark:bg-primary-900/20 text-primary-600'
                  : 'text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700'
              ]"
            >
              全部
            </NuxtLink>
            <NuxtLink
              v-for="category in categories"
              :key="category.categoryId"
              :to="`/forum?category=${category.categoryId}`"
              :class="[
                'block px-3 py-2 rounded-lg transition-colors',
                route.query.category === category.categoryId
                  ? 'bg-primary-50 dark:bg-primary-900/20 text-primary-600'
                  : 'text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700'
              ]"
            >
              {{ category.name }}
            </NuxtLink>
          </nav>
        </div>
      </aside>

      <!-- Main Content -->
      <div class="flex-1">
        <div class="flex items-center justify-between mb-6">
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
            {{ $t('forum.title') }}
          </h1>
          <NuxtLink
            v-if="authStore.isAuthenticated"
            to="/forum/new"
            class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
          >
            {{ $t('forum.newPost') }}
          </NuxtLink>
        </div>

        <!-- Sort Tabs -->
        <div class="flex gap-2 mb-6">
          <button
            v-for="sort in sortOptions"
            :key="sort.value"
            @click="currentSort = sort.value"
            :class="[
              'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
              currentSort === sort.value
                ? 'bg-primary-600 text-white'
                : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700'
            ]"
          >
            {{ sort.label }}
          </button>
        </div>

        <!-- Posts List -->
        <div class="space-y-4">
          <NuxtLink
            v-for="post in posts"
            :key="post.postId"
            :to="`/forum/${post.postId}`"
            class="block bg-white dark:bg-gray-800 rounded-lg shadow hover:shadow-md transition-shadow p-4"
          >
            <div class="flex items-start gap-4">
              <img
                :src="post.authorAvatar || '/default-avatar.png'"
                :alt="post.authorName"
                class="w-10 h-10 rounded-full"
              />
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <span v-if="post.isPinned" class="px-2 py-0.5 bg-red-100 dark:bg-red-900/20 text-red-600 text-xs rounded">
                    置顶
                  </span>
                  <span v-if="post.isFeatured" class="px-2 py-0.5 bg-yellow-100 dark:bg-yellow-900/20 text-yellow-600 text-xs rounded">
                    精华
                  </span>
                  <h2 class="font-semibold text-gray-900 dark:text-white truncate">
                    {{ post.title }}
                  </h2>
                </div>
                <div class="flex items-center gap-4 text-sm text-gray-500">
                  <span>{{ post.authorName }}</span>
                  <span>{{ formatDate(post.createdAt) }}</span>
                  <span class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                    {{ post.viewCount }}
                  </span>
                  <span class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                    </svg>
                    {{ post.replyCount }}
                  </span>
                </div>
              </div>
            </div>
          </NuxtLink>
        </div>

        <!-- Empty State -->
        <div v-if="!posts.length" class="text-center py-20">
          <p class="text-gray-500 dark:text-gray-400">暂无帖子</p>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="mt-8 flex justify-center">
          <nav class="flex items-center space-x-2">
            <button
              :disabled="currentPage === 1"
              @click="currentPage--"
              class="px-3 py-2 rounded-lg bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 disabled:opacity-50"
            >
              {{ $t('common.previous') }}
            </button>
            <span class="px-4 py-2 text-gray-600 dark:text-gray-300">
              {{ currentPage }} / {{ totalPages }}
            </span>
            <button
              :disabled="currentPage === totalPages"
              @click="currentPage++"
              class="px-3 py-2 rounded-lg bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 disabled:opacity-50"
            >
              {{ $t('common.next') }}
            </button>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ForumCategory, ForumPost } from '~/types'

const { t } = useI18n()
const route = useRoute()
const authStore = useAuthStore()

const currentSort = ref('LATEST')
const currentPage = ref(1)
const totalPages = ref(1)

const sortOptions = [
  { value: 'LATEST', label: t('forum.latestPosts') },
  { value: 'HOT', label: t('forum.hotPosts') },
]

// Mock data
const categories = ref<ForumCategory[]>([
  { categoryId: '1', name: '公告', description: '', icon: '', order: 1, postCount: 10, lastPostAt: null },
  { categoryId: '2', name: '技术讨论', description: '', icon: '', order: 2, postCount: 50, lastPostAt: null },
  { categoryId: '3', name: '资源分享', description: '', icon: '', order: 3, postCount: 30, lastPostAt: null },
  { categoryId: '4', name: '反馈建议', description: '', icon: '', order: 4, postCount: 20, lastPostAt: null },
])

const posts = ref<ForumPost[]>([
  {
    postId: '1',
    title: 'Azathoth 2.0 正式发布！',
    content: '',
    authorId: 'admin',
    authorName: 'Admin',
    authorAvatar: null,
    categoryId: '1',
    tags: ['公告'],
    isPinned: true,
    isFeatured: true,
    isLocked: false,
    viewCount: 10000,
    likeCount: 500,
    replyCount: 100,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
    lastReplyAt: '2024-01-02T00:00:00Z',
  },
  {
    postId: '2',
    title: '如何开发自定义技能系统',
    content: '',
    authorId: 'dev1',
    authorName: 'Developer',
    authorAvatar: null,
    categoryId: '2',
    tags: ['教程', '技能'],
    isPinned: false,
    isFeatured: true,
    isLocked: false,
    viewCount: 500,
    likeCount: 50,
    replyCount: 20,
    createdAt: '2024-01-02T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
    lastReplyAt: '2024-01-03T00:00:00Z',
  },
])

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

useHead({
  title: t('forum.title') + ' - Azathoth',
})
</script>
