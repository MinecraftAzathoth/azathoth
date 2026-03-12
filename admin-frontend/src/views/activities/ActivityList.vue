<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchActivities } from '../../api/admin'
import type { ActivityInfo } from '../../types'

const activities = ref<ActivityInfo[]>([])

onMounted(async () => {
  activities.value = await fetchActivities()
})

const stateMap: Record<string, { label: string; cls: string }> = {
  SCHEDULED: { label: '已排期', cls: 'badge-blue' },
  PREPARING: { label: '准备中', cls: 'badge-yellow' },
  ACTIVE: { label: '进行中', cls: 'badge-green' },
  ENDING: { label: '即将结束', cls: 'badge-orange' },
  ENDED: { label: '已结束', cls: 'badge-gray' },
  CANCELLED: { label: '已取消', cls: 'badge-gray' },
}

const typeMap: Record<string, string> = {
  LIMITED_TIME: '限时',
  RECURRING: '周期',
  PERMANENT: '常驻',
  SPECIAL_EVENT: '特殊',
  SEASONAL: '赛季',
}

function formatDate(d: string) {
  return new Date(d).toLocaleString('zh-CN')
}
</script>

<template>
  <div class="activity-list">
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>活动名称</th>
            <th>类型</th>
            <th>状态</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th>参与人数</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="act in activities" :key="act.activityId">
            <td>
              <div>{{ act.name }}</div>
              <div class="desc">{{ act.description }}</div>
            </td>
            <td>{{ typeMap[act.type] || act.type }}</td>
            <td><span class="badge" :class="stateMap[act.state]?.cls">{{ stateMap[act.state]?.label || act.state }}</span></td>
            <td>{{ formatDate(act.startTime) }}</td>
            <td>{{ formatDate(act.endTime) }}</td>
            <td>{{ act.participantCount.toLocaleString() }}</td>
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
.desc { font-size: 0.8rem; color: var(--text-muted); margin-top: 2px; }
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
.badge-orange { background: #ffedd5; color: #9a3412; }
.badge-gray { background: #f3f4f6; color: #6b7280; }
</style>
