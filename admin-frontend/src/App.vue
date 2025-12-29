<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useAuthStore } from './stores/auth'
import AppLayout from './layouts/AppLayout.vue'
import BlankLayout from './layouts/BlankLayout.vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const authStore = useAuthStore()

const layout = computed(() => {
  return route.meta.layout === 'blank' ? BlankLayout : AppLayout
})
</script>

<template>
  <component :is="layout">
    <RouterView />
  </component>
</template>

<style>
:root {
  --primary-color: #4f46e5;
  --primary-hover: #4338ca;
  --bg-color: #f9fafb;
  --sidebar-bg: #1f2937;
  --text-color: #111827;
  --text-muted: #6b7280;
  --border-color: #e5e7eb;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: var(--bg-color);
  color: var(--text-color);
}

#app {
  min-height: 100vh;
}
</style>
