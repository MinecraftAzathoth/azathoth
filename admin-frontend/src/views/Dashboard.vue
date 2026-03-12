<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchPlayerStats, fetchInstanceStats } from '../api/admin'
import type { PlayerStats, InstanceStats } from '../types'

const playerStats = ref<PlayerStats | null>(null)
const instanceStats = ref<InstanceStats | null>(null)
const tps = ref(19.8)
const memoryUsage = ref('12.4 / 32 GB')

onMounted(async () => {
  playerStats.value = await fetchPlayerStats()
  instanceStats.value = await fetchInstanceStats()
})

const cards = [
  { key: 'online', label: '在线玩家', color: '#10b981' },
  { key: 'instances', label: '活跃实例', color: '#3b82f6' },
  { key: 'tps', label: 'TPS', color: '#f59e0b' },
  { key: 'memory', label: '内存使用', color: '#8b5cf6' },
]

function cardValue(key: string): string {
  if (!playerStats.value || !instanceStats.value) return '...'
  switch (key) {
    case 'online': return playerStats.value.onlinePlayers.toLocaleString()
    case 'instances': return String(instanceStats.value.activeInstances)
    case 'tps': return tps.value.toFixed(1)
    case 'memory': return memoryUsage.value
    default: return '-'
  }
}
</script>

<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div v-for="card in cards" :key="card.key" class="stat-card">
        <div class="stat-indicator" :style="{ background: card.color }"></div>
        <div class="stat-body">
          <span class="stat-label">{{ card.label }}</span>
          <span class="stat-value">{{ cardValue(card.key) }}</span>
        </div>
      </div>
    </div>

    <div class="info-grid" v-if="playerStats && instanceStats">
      <div class="info-card">
        <h3>玩家概览</h3>
        <ul class="info-list">
          <li><span>总玩家数</span><span>{{ playerStats.totalPlayers.toLocaleString() }}</span></li>
          <li><span>今日新增</span><span>{{ playerStats.newPlayersToday }}</span></li>
          <li><span>本周新增</span><span>{{ playerStats.newPlayersThisWeek }}</span></li>
          <li><span>今日活跃</span><span>{{ playerStats.activePlayersToday.toLocaleString() }}</span></li>
        </ul>
      </div>
      <div class="info-card">
        <h3>实例概览</h3>
        <ul class="info-list">
          <li><span>总实例数</span><span>{{ instanceStats.totalInstances }}</span></li>
          <li><span>CPU 使用率</span><span>{{ instanceStats.cpuUsage }}%</span></li>
          <li><span>内存使用率</span><span>{{ instanceStats.memoryUsage }}%</span></li>
          <li v-for="(count, type) in instanceStats.byType" :key="type"><span>{{ type }}</span><span>{{ count }}</span></li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 28px;
}
.stat-card {
  background: white;
  border-radius: 10px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.stat-indicator {
  width: 8px;
  height: 48px;
  border-radius: 4px;
}
.stat-body {
  display: flex;
  flex-direction: column;
}
.stat-label {
  font-size: 0.85rem;
  color: var(--text-muted);
}
.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  margin-top: 2px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 20px;
}
.info-card {
  background: white;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.info-card h3 {
  font-size: 1rem;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}
.info-list {
  list-style: none;
}
.info-list li {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 0.9rem;
  border-bottom: 1px solid #f3f4f6;
}
.info-list li:last-child {
  border-bottom: none;
}
.info-list li span:last-child {
  font-weight: 600;
}
</style>
