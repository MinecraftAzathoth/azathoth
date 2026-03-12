<script setup lang="ts">
import { ref } from 'vue'

const categories = ['server', 'game', 'gateway', 'database', 'kafka']
const selectedCategory = ref('server')

const configs: Record<string, string> = {
  server: JSON.stringify({
    maxPlayers: 100000,
    tickRate: 20,
    viewDistance: 10,
    simulationDistance: 8,
    motd: 'Azathoth MMORPG Server',
  }, null, 2),
  game: JSON.stringify({
    expMultiplier: 1.5,
    dropRateMultiplier: 1.0,
    maxLevel: 100,
    pvpEnabled: true,
    guildMaxMembers: 50,
  }, null, 2),
  gateway: JSON.stringify({
    port: 25565,
    compressionThreshold: 256,
    rateLimitPerSecond: 100,
    maxConnectionsPerIp: 5,
    timeout: 30000,
  }, null, 2),
  database: JSON.stringify({
    postgres: { host: 'localhost', port: 5432, database: 'azathoth', poolSize: 20 },
    mongodb: { uri: 'mongodb://localhost:27017', database: 'azathoth_logs' },
    redis: { host: 'localhost', port: 6379, database: 0 },
  }, null, 2),
  kafka: JSON.stringify({
    bootstrapServers: 'localhost:9092',
    groupId: 'azathoth-game',
    autoOffsetReset: 'latest',
    topics: { playerEvents: 'player-events', chatMessages: 'chat-messages', tradeEvents: 'trade-events' },
  }, null, 2),
}

const content = ref(configs[selectedCategory.value])
const saved = ref(false)

function switchCategory(cat: string) {
  selectedCategory.value = cat
  content.value = configs[cat]
  saved.value = false
}

function save() {
  configs[selectedCategory.value] = content.value
  saved.value = true
  setTimeout(() => { saved.value = false }, 2000)
}
</script>

<template>
  <div class="config-editor">
    <div class="sidebar-tabs">
      <button
        v-for="cat in categories"
        :key="cat"
        class="tab-btn"
        :class="{ active: selectedCategory === cat }"
        @click="switchCategory(cat)"
      >
        {{ cat }}
      </button>
    </div>
    <div class="editor-area">
      <div class="editor-header">
        <span class="editor-title">{{ selectedCategory }}.json</span>
        <div class="editor-actions">
          <span v-if="saved" class="save-msg">已保存 ✓</span>
          <button class="btn" @click="save">保存</button>
        </div>
      </div>
      <textarea v-model="content" class="code-textarea" spellcheck="false"></textarea>
    </div>
  </div>
</template>

<style scoped>
.config-editor {
  display: flex;
  gap: 20px;
  height: calc(100vh - 140px);
}
.sidebar-tabs {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 140px;
}
.tab-btn {
  padding: 10px 16px;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  cursor: pointer;
  text-align: left;
  font-size: 0.9rem;
  text-transform: capitalize;
  transition: all 0.15s;
}
.tab-btn:hover { background: #f3f4f6; }
.tab-btn.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}
.editor-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}
.editor-title { font-weight: 600; font-size: 0.9rem; }
.editor-actions { display: flex; align-items: center; gap: 12px; }
.save-msg { color: #10b981; font-size: 0.85rem; }
.btn {
  padding: 6px 16px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
}
.btn:hover { background: var(--primary-hover); }
.code-textarea {
  flex: 1;
  padding: 16px;
  border: none;
  resize: none;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 0.9rem;
  line-height: 1.6;
  background: #1e1e2e;
  color: #cdd6f4;
  outline: none;
}
</style>
