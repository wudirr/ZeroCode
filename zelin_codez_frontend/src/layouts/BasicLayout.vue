<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader v-if="!route.meta.hideHeader" />

    <!-- 主要内容区域 -->
    <a-layout-content class="main-content" :class="{ 'no-header': route.meta.hideHeader }">
      <router-view />
    </a-layout-content>

    <!-- 底部版权信息 -->
    <GlobalFooter v-if="!route.meta.hideHeader" />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<style scoped>
.basic-layout {
  background: none;
  min-height: 100vh;
}

.main-content {
  padding: 0;
  margin: 0;
}

.main-content.no-header {
  height: 100vh;
}
</style>

<style>
/* 仅在聊天页面隐藏全局滚动条 */
.no-header ~ global-scope {
  body,
  html {
    overflow: hidden !important;
  }
}
</style>
