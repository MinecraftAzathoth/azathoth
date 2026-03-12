<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchInstances } from '../../api/admin'
import type { InstanceInfo } from '../../types'

const instances = ref<InstanceInfo[]>([])

onMounted(async () => {
  instances.value = await fetchInstances()
})

const stateMap: Record<string, { label: string; cls: string }> = {
  CREATING: { label: '创建中', cls: 'badge-yellow' },
  WAITING: { label: '等待中', cls: 'badge-blue' },
  IN_PROGRESS: { label: '运行中', cls: 'badge-green' },
  COMPLETED: { label: '已完成', cls: 'badge-gray' },
  CLOSED: { label: '已关闭', cls: 'badge-gray' },
}

const typeMap: Record<string, string> = {
  MAIN_CITY: '主城',
  DUNGEON: '副本',
  ARENA: '竞技场',
  EVENT: '活动',
}
</script>

<template>
  <div class="instance-list">
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>名称</th>
            <th>类型</th>
            <th>状态</th>
            <th>玩家数</th>
            <th>CPU</th>
            <th>内存</th>
            <th>区域</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="inst in instances" :key="inst.instanceId">
            <td>{{ inst.templateName || inst.instanceId }}</td>
            <td>{{ typeMap[inst.instanceType] || inst.instanceType }}</td>
            <td><span class="badge" :class="stateMap[inst.state]?.cls">{{ stateMap[inst.state]?.label || inst.state }}</span></td>
            <td>{{ inst.playerCount }} / {{ inst.maxPlayers }}</td>
            <td>{{ inst.cpu }}%</td>
            <td>{{ inst.memory }}%</td>
            <td>{{ inst.region }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.table-wrap {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
table { width: 100%; border-collapse: collapse; }
th, td { padding: 12px 16px; text-align: left; font-size: 0.9rem; }
th { background: #f9fafb; font-weight: 600; color: var(--text-muted); border-bottom: 1px solid var(--border-color); }
td { border-bottom: 1px solid #f3f4f6; }
.badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}
.badge-green { background: #d1fae5; color: #065f46; }
.badge-blue { background: #dbeafe; color: #1e40af; }
.badge-yellow { background: #fef3c7; color: #92400e; }
.badge-gray { background: #f3f4f6; color: #6b7280; }
</style>
