<template>
  <div class="ChatView">
    <div class="chat-container">
      <div class="chat-header">
        <a-button @click="goBack" type="text" class="back-btn">
          <LeftOutlined />
          返回
        </a-button>
        <h2>AI 对话 - {{ appId }}</h2>
      </div>
      <div class="chat-content">
        <a-empty description="AI 对话功能正在开发中..." />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { LeftOutlined } from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()

const appId = route.params.appId

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.ChatView {
  min-height: 100vh;
  padding: 24px;
}

.chat-container {
  max-width: 1200px;
  margin: 0 auto;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.chat-content {
  background: #fff;
  border-radius: 12px;
  padding: 48px;
  min-height: 400px;
}
</style>
