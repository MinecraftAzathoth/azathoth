<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchAnnouncements } from '../../api/admin'
import type { AnnouncementInfo } from '../../types'

const announcements = ref<AnnouncementInfo[]>([])
const editing = ref<AnnouncementInfo | null>(null)
const showForm = ref(false)

const formTitle = ref('')
const formContent = ref('')
const formType = ref<AnnouncementInfo['type']>('NORMAL')

onMounted(async () => {
  announcements.value = await fetchAnnouncements()
})

const typeMap: Record<string, { label: string; cls: string }> = {
  NORMAL: { label: '普通', cls: 'badge-blue' },
  IMPORTANT: { label: '重要', cls: 'badge-red' },
  MAINTENANCE: { label: '维护', cls: 'badge-yellow' },
  EVENT: { label: '活动', cls: 'badge-green' },
}

function formatDate(d?: string) {
  return d ? new Date(d).toLocaleString('zh-CN') : '-'
}

function openCreate() {
  editing.value = null
  formTitle.value = ''
  formContent.value = ''
  formType.value = 'NORMAL'
  showForm.value = true
}

function openEdit(ann: AnnouncementInfo) {
  editing.value = ann
  formTitle.value = ann.title
  formContent.value = ann.content
  formType.value = ann.type
  showForm.value = true
}

function saveForm() {
  if (editing.value) {
    editing.value.title = formTitle.value
    editing.value.content = formContent.value
    editing.value.type = formType.value
  } else {
    announcements.value.unshift({
      announcementId: `ann-${Date.now()}`,
      title: formTitle.value,
      content: formContent.value,
      type: formType.value,
      publishedAt: new Date().toISOString(),
      published: true,
      authorId: 'admin-001',
      authorName: '管理员',
    })
  }
  showForm.value = false
}

function deleteAnn(id: string) {
  announcements.value = announcements.value.filter((a) => a.announcementId !== id)
}
</script>

<template>
  <div class="announcement-list">
    <div class="toolbar">
      <button class="btn" @click="openCreate">新建公告</button>
    </div>

    <div v-if="showForm" class="form-card">
      <h3>{{ editing ? '编辑公告' : '新建公告' }}</h3>
      <div class="form-group">
        <label>标题</label>
        <input v-model="formTitle" />
      </div>
      <div class="form-group">
        <label>类型</label>
        <select v-model="formType">
          <option value="NORMAL">普通</option>
          <option value="IMPORTANT">重要</option>
          <option value="MAINTENANCE">维护</option>
          <option value="EVENT">活动</option>
        </select>
      </div>
      <div class="form-group">
        <label>内容</label>
        <textarea v-model="formContent" rows="4"></textarea>
      </div>
      <div class="form-actions">
        <button class="btn" @click="saveForm">保存</button>
        <button class="btn btn-secondary" @click="showForm = false">取消</button>
      </div>
    </div>

    <div class="cards">
      <div v-for="ann in announcements" :key="ann.announcementId" class="ann-card">
        <div class="ann-header">
          <span class="badge" :class="typeMap[ann.type]?.cls">{{ typeMap[ann.type]?.label }}</span>
          <span class="ann-date">{{ formatDate(ann.publishedAt) }}</span>
        </div>
        <h4 class="ann-title">{{ ann.title }}</h4>
        <p class="ann-content">{{ ann.content }}</p>
        <div class="ann-footer">
          <span class="ann-author">{{ ann.authorName }}</span>
          <div class="ann-actions">
            <button class="btn-link" @click="openEdit(ann)">编辑</button>
            <button class="btn-link btn-link-red" @click="deleteAnn(ann.announcementId)">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.toolbar { margin-bottom: 20px; }
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
.btn-secondary { background: #6b7280; }
.btn-secondary:hover { background: #4b5563; }
.form-card {
  background: white;
  border-radius: 10px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.form-card h3 { margin-bottom: 16px; font-size: 1rem; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; margin-bottom: 4px; font-size: 0.85rem; font-weight: 500; }
.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 0.9rem;
  font-family: inherit;
}
.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--primary-color);
}
.form-actions { display: flex; gap: 10px; }
.cards { display: flex; flex-direction: column; gap: 16px; }
.ann-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.ann-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.ann-date { font-size: 0.8rem; color: var(--text-muted); }
.ann-title { font-size: 1rem; margin-bottom: 8px; }
.ann-content { font-size: 0.9rem; color: var(--text-muted); line-height: 1.5; margin-bottom: 12px; }
.ann-footer { display: flex; justify-content: space-between; align-items: center; }
.ann-author { font-size: 0.8rem; color: var(--text-muted); }
.ann-actions { display: flex; gap: 12px; }
.btn-link { background: none; border: none; color: var(--primary-color); cursor: pointer; font-size: 0.85rem; }
.btn-link:hover { text-decoration: underline; }
.btn-link-red { color: #dc2626; }
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
.badge-red { background: #fee2e2; color: #991b1b; }
</style>
