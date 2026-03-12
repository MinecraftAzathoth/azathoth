<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { fetchPlayers } from '../../api/admin'
import type { PlayerInfo, PagedResponse } from '../../types'

const router = useRouter()
const search = ref('')
const page = ref(1)
const pageSize = 10
const data = ref<PagedResponse<PlayerInfo> | null>(null)

async function load() {
  data.value = await fetchPlayers(page.value, pageSize, search.value)
}

onMounted(load)
watch([page], load)

function doSearch() {
  page.value = 1
  load()
}

function viewPlayer(id: string) {
  router.push(`/players/${id}`)
}

function formatDate(d?: string) {
  return d ? new Date(d).toLocaleString('zh-CN') : '-'
}
</script>

<template>
  <div class="player-list">
    <div class="toolbar">
      <input v-model="search" placeholder="搜索玩家..." class="search-input" @keyup.enter="doSearch" />
      <button class="btn" @click="doSearch">搜索</button>
    </div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>玩家名</th>
            <th>等级</th>
            <th>状态</th>
            <th>最后登录</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in data?.items" :key="p.playerId">
            <td>{{ p.displayName }} <span class="username-sub">@{{ p.username }}</span></td>
            <td>Lv.{{ p.level }}</td>
            <td><span class="badge" :class="p.online ? 'badge-green' : 'badge-gray'">{{ p.online ? '在线' : '离线' }}</span></td>
            <td>{{ formatDate(p.lastLoginAt) }}</td>
            <td><button class="btn-link" @click="viewPlayer(p.playerId)">详情</button></td>
          </tr>
          <tr v-if="data && data.items.length === 0">
            <td colspan="5" class="empty">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="data && data.totalPages > 1">
      <button class="btn-sm" :disabled="page <= 1" @click="page--">上一页</button>
      <span class="page-info">{{ page }} / {{ data.totalPages }}</span>
      <button class="btn-sm" :disabled="page >= data.totalPages" @click="page++">下一页</button>
    </div>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.search-input {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  width: 280px;
  font-size: 0.9rem;
}
.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
}
.btn {
  padding: 8px 20px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}
.btn:hover { background: var(--primary-hover); }
.table-wrap {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
table {
  width: 100%;
  border-collapse: collapse;
}
th, td {
  padding: 12px 16px;
  text-align: left;
  font-size: 0.9rem;
}
th {
  background: #f9fafb;
  font-weight: 600;
  color: var(--text-muted);
  border-bottom: 1px solid var(--border-color);
}
td {
  border-bottom: 1px solid #f3f4f6;
}
.username-sub {
  color: var(--text-muted);
  font-size: 0.8rem;
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
.btn-link {
  background: none;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  font-size: 0.9rem;
}
.btn-link:hover { text-decoration: underline; }
.empty {
  text-align: center;
  color: var(--text-muted);
  padding: 32px;
}
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 20px;
}
.btn-sm {
  padding: 6px 14px;
  border: 1px solid var(--border-color);
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
}
.btn-sm:disabled { opacity: 0.4; cursor: not-allowed; }
.page-info { font-size: 0.9rem; color: var(--text-muted); }
</style>
