<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const remember = ref(false)
const error = ref('')

async function handleLogin() {
  error.value = ''
  if (!username.value || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
  const result = await authStore.login(username.value, password.value, remember.value)
  if (result.success) {
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } else {
    error.value = result.error || '登录失败'
  }
}
</script>

<template>
  <div class="login-card">
    <h2 class="login-title">Azathoth Admin</h2>
    <p class="login-subtitle">管理后台登录</p>
    <form @submit.prevent="handleLogin" class="login-form">
      <div class="form-group">
        <label for="username">用户名</label>
        <input id="username" v-model="username" type="text" placeholder="请输入用户名" autocomplete="username" />
      </div>
      <div class="form-group">
        <label for="password">密码</label>
        <input id="password" v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" />
      </div>
      <div class="form-check">
        <label><input type="checkbox" v-model="remember" /> 记住我</label>
      </div>
      <div v-if="error" class="error-msg">{{ error }}</div>
      <button type="submit" class="login-btn" :disabled="authStore.loading">
        {{ authStore.loading ? '登录中...' : '登 录' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.login-card {
  background: white;
  border-radius: 12px;
  padding: 48px 40px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}
.login-title {
  text-align: center;
  font-size: 1.75rem;
  color: var(--primary-color);
  margin-bottom: 4px;
}
.login-subtitle {
  text-align: center;
  color: var(--text-muted);
  margin-bottom: 32px;
}
.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 0.875rem;
  font-weight: 500;
}
.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 0.95rem;
  transition: border-color 0.2s;
}
.form-group input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}
.form-check {
  margin-bottom: 20px;
  font-size: 0.875rem;
  color: var(--text-muted);
}
.form-check input {
  margin-right: 6px;
}
.error-msg {
  background: #fef2f2;
  color: #dc2626;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 0.875rem;
  margin-bottom: 16px;
}
.login-btn {
  width: 100%;
  padding: 12px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.login-btn:hover:not(:disabled) {
  background: var(--primary-hover);
}
.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
