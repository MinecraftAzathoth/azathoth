<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchPlayer } from '../../api/admin'
import type { PlayerInfo } from '../../types'

const route = useRoute()
const router = useRouter()
const player = ref<PlayerInfo | null>(null)

onMounted(async () => {
  player.value = await fetchPlayer(route.params.id as string)
})

function formatDate(d?: string) {
  return d ? new Date(d).toLocaleString('zh-CN') : '-'
}

function toggleBan() {
  if (!player.value) return
  if (player.value.banStatus?.banned) {
    player.value.banStatus = { banned: false }
  } else {
    player.value.banStatus = { banned: true, reason: '管理员操作', bannedAt: new Date().toISOString(), bannedBy: 'admin' }
  }
}
</script>

<template>
  <div class="player-detail" v-if="player">
    <button class="btn-back" @click="router.push('/players')">← 返回列表</button>

    <div class="detail-grid">
      <div class="card">
        <h3>基本信息</h3>
        <dl class="info-dl">
          <dt>用户名</dt><dd>{{ player.username }}</dd>
          <dt>显示名</dt><dd>{{ player.displayName }}</dd>
          <dt>UUID</dt><dd class="mono">{{ player.playerId }}</dd>
          <dt>等级</dt><dd>Lv.{{ player.level }}</dd>
          <dt>经验值</dt><dd>{{ player.experience.toLocaleString() }}</dd>
          <dt>金币</dt><dd>{{ player.gold.toLocaleString() }}</dd>
          <dt>钻石</dt><dd>{{ player.diamond.toLocaleString() }}</dd>
          <dt>VIP 等级</dt><dd>VIP {{ player.vipLevel }}</dd>
          <dt>公会</dt><dd>{{ player.guildName || '无' }}</dd>
          <dt>注册时间</dt><dd>{{ formatDate(player.createdAt) }}</dd>
          <dt>最后登录</dt><dd>{{ formatDate(player.lastLoginAt) }}</dd>
        </dl>
      </div>

      <div class="card">
        <h3>状态 & 操作</h3>
        <div class="status-section">
          <div class="status-row">
            <span>在线状态</span>
            <span class="badge" :class="player.online ? 'badge-green' : 'badge-gray'">{{ player.online ? '在线' : '离线' }}</span>
          </div>
          <div class="status-row" v-if="player.online">
            <span>当前实例</span>
            <span class="mono">{{ player.currentInstance }}</span>
          </div>
          <div class="status-row">
            <span>封禁状态</span>
            <span class="badge" :class="player.banStatus?.banned ? 'badge-red' : 'badge-green'">{{ player.banStatus?.banned ? '已封禁' : '正常' }}</span>
          </div>
          <div v-if="player.banStatus?.banned" class="ban-info">
            <p>原因: {{ player.banStatus.reason }}</p>
            <p>封禁时间: {{ formatDate(player.banStatus.bannedAt) }}</p>
            <p>操作人: {{ player.banStatus.bannedBy }}</p>
          </div>
        </div>
        <div class="actions">
          <button class="btn" :class="player.banStatus?.banned ? 'btn-green' : 'btn-red'" @click="toggleBan">
            {{ player.banStatus?.banned ? '解除封禁' : '封禁玩家' }}
          </button>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="loading">加载中...</div>
</template>

<style scoped>
.btn-back {
  background: none;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  font-size: 0.9rem;
  margin-bottom: 20px;
  padding: 0;
}
.btn-back:hover { text-decoration: underline; }
.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: 20px;
}
.card {
  background: white;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.card h3 {
  font-size: 1rem;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}
.info-dl {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 8px 12px;
  font-size: 0.9rem;
}
.info-dl dt { color: var(--text-muted); }
.info-dl dd { font-weight: 500; }
.mono { font-family: monospace; font-size: 0.85rem; }
.status-section { margin-bottom: 20px; }
.status-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-size: 0.9rem;
  border-bottom: 1px solid #f3f4f6;
}
.badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}
.badge-green { background: #d1fae5; color: #065f46; }
.badge-gray { background: #f3f4f6; color: #6b7280; }
.badge-red { background: #fee2e2; color: #991b1b; }
.ban-info {
  background: #fef2f2;
  padding: 12px;
  border-radius: 6px;
  margin-top: 12px;
  font-size: 0.85rem;
  color: #991b1b;
}
.ban-info p { margin-bottom: 4px; }
.actions { margin-top: 16px; }
.btn {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  color: white;
}
.btn-red { background: #dc2626; }
.btn-red:hover { background: #b91c1c; }
.btn-green { background: #10b981; }
.btn-green:hover { background: #059669; }
.loading {
  text-align: center;
  padding: 48px;
  color: var(--text-muted);
}
</style>
