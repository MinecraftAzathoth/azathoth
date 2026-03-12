<script setup lang="ts">
import { ref, onMounted, watch, shallowRef } from 'vue'
import * as echarts from 'echarts'
import { fetchAnalytics } from '../../api/admin'
import type { AnalyticsData } from '../../types'

const lineChartEl = ref<HTMLDivElement>()
const pieChartEl = ref<HTMLDivElement>()
const lineChart = shallowRef<echarts.ECharts>()
const pieChart = shallowRef<echarts.ECharts>()
const data = ref<AnalyticsData[]>([])

onMounted(async () => {
  data.value = await fetchAnalytics()

  if (lineChartEl.value) {
    lineChart.value = echarts.init(lineChartEl.value)
  }
  if (pieChartEl.value) {
    pieChart.value = echarts.init(pieChartEl.value)
  }

  renderCharts()
})

watch(data, renderCharts)

function renderCharts() {
  if (!data.value.length) return

  const hours = data.value.map((d) => {
    const h = new Date(d.timestamp).getHours()
    return `${String(h).padStart(2, '0')}:00`
  })

  lineChart.value?.setOption({
    title: { text: '在线玩家趋势', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: hours, boundaryGap: false },
    yAxis: { type: 'value' },
    grid: { left: 60, right: 30, top: 50, bottom: 30 },
    series: [
      {
        name: '在线玩家',
        type: 'line',
        data: data.value.map((d) => d.onlinePlayers),
        smooth: true,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#4f46e5' },
      },
      {
        name: '活跃实例',
        type: 'line',
        data: data.value.map((d) => d.activeInstances),
        smooth: true,
        yAxisIndex: 0,
        itemStyle: { color: '#10b981' },
      },
    ],
  })

  pieChart.value?.setOption({
    title: { text: '实例类型分布', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '48%'],
        data: [
          { value: 8, name: '主城' },
          { value: 18, name: '副本' },
          { value: 4, name: '竞技场' },
          { value: 2, name: '活动' },
        ],
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' } },
      },
    ],
  })
}

// 响应窗口大小变化
window.addEventListener('resize', () => {
  lineChart.value?.resize()
  pieChart.value?.resize()
})
</script>

<template>
  <div class="analytics">
    <div class="chart-grid">
      <div class="chart-card chart-wide">
        <div ref="lineChartEl" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div ref="pieChartEl" class="chart-container"></div>
      </div>
    </div>

    <div class="summary-card">
      <h3>今日汇总</h3>
      <div class="summary-grid" v-if="data.length">
        <div class="summary-item">
          <span class="summary-label">峰值在线</span>
          <span class="summary-value">{{ Math.max(...data.map(d => d.onlinePlayers)).toLocaleString() }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">总交易数</span>
          <span class="summary-value">{{ data.reduce((s, d) => s + d.transactions, 0).toLocaleString() }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">总收入</span>
          <span class="summary-value">¥{{ data.reduce((s, d) => s + d.revenue, 0).toLocaleString() }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">平均在线</span>
          <span class="summary-value">{{ Math.round(data.reduce((s, d) => s + d.onlinePlayers, 0) / data.length).toLocaleString() }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chart-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}
.chart-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.chart-container {
  width: 100%;
  height: 360px;
}
.summary-card {
  background: white;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.summary-card h3 {
  font-size: 1rem;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}
.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
}
.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.summary-label { font-size: 0.85rem; color: var(--text-muted); }
.summary-value { font-size: 1.5rem; font-weight: 700; }
</style>
