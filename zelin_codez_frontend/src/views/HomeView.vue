<template>
  <div class="home-view">
    <!-- 动态渐变背景 -->
    <div class="bg-gradient">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 对话输入区 -->
      <section class="chat-section">
        <div class="chat-title">
          <h1>
            一句话
            <img src="@/favicon.ico" alt="Logo" class="title-logo" />
            呈所想
          </h1>
          <p class="subtitle">与 AI 对话轻松创建应用和网站</p>
        </div>

        <div class="chat-input-wrapper">
          <div class="input-box">
            <div class="input-content">
              <div class="input-main">
                <SparklesIcon class="input-icon" />
                <textarea
                  v-model="prompt"
                  placeholder="描述你想要的应用..."
                  class="custom-textarea"
                  rows="3"
                  @keydown.enter="handleCreateApp"
                ></textarea>
              </div>
              <div class="input-footer">
                <div class="input-footer-left">
                  <a-select v-model:value="codeGenType" class="type-select">
                    <a-select-option value="html">HTML</a-select-option>
                    <a-select-option value="multi_file">多文件</a-select-option>
                  </a-select>
                </div>
                <div class="input-footer-right">
                  <a-button
                    type="primary"
                    :loading="creating"
                    :disabled="!prompt.trim()"
                    @click="handleCreateApp"
                    class="create-btn-circle"
                    :class="{ 'has-content': prompt.trim() }"
                  >
                    <template #icon>
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2.5"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        class="arrow-up-icon"
                        :class="{ active: prompt.trim() }"
                      >
                        <path d="M12 19V5M5 12l7-7 7 7" />
                      </svg>
                    </template>
                  </a-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 推荐主题 -->
          <div class="recommend-topics">
            <span class="label">试试这些:</span>
            <div class="topics">
              <a-button
                v-for="topic in recommendTopics"
                :key="topic.prompt"
                class="topic-btn"
                @click="selectTopic(topic.prompt)"
              >
                <component :is="topic.icon" />
                {{ topic.label }}
              </a-button>
            </div>
          </div>
        </div>
      </section>

      <!-- 内容展示区 -->
      <section class="content-section">
        <div class="section-card">
          <!-- 我的作品 -->
          <div class="content-block">
            <div class="section-header">
              <FolderOpenIcon class="section-icon" />
              <h2>我的作品</h2>
            </div>
            <a-row :gutter="[16, 20]" v-if="myApps.length > 0">
              <a-col :span="6" v-for="app in myApps" :key="app.id">
                <div class="app-item">
                  <div class="app-card">
                    <div class="app-cover">
                      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
                      <div v-else class="default-cover">
                        <AppstoreIcon />
                      </div>
                      <div class="card-overlay">
                        <a-button type="primary" @click.stop="goToChat(app.id)">
                          <template #icon><MessageIcon /></template>
                          查看对话
                        </a-button>
                      </div>
                    </div>
                  </div>
                  <div class="app-info">
                    <h3>{{ app.appName }}</h3>
                    <p>{{ formatTime(app.createTime) }}</p>
                  </div>
                </div>
              </a-col>
            </a-row>
            <a-empty v-else description="暂无作品" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
          </div>

          <!-- 分隔线 -->
          <a-divider />

          <!-- 精选案例 -->
          <div class="content-block">
            <div class="section-header">
              <StarIcon class="section-icon featured" />
              <h2>精选案例</h2>
            </div>
            <a-row :gutter="[16, 20]" v-if="featuredApps.length > 0">
              <a-col :span="6" v-for="app in featuredApps" :key="app.id">
                <div class="app-item">
                  <div class="app-card">
                    <div class="app-cover">
                      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
                      <div v-else class="default-cover">
                        <AppstoreIcon />
                      </div>
                      <div class="card-overlay">
                        <a-button type="primary" @click="previewApp(app)">
                          <template #icon><EyeIcon /></template>
                          预览
                        </a-button>
                        <a-button v-if="app.deployKey" @click="viewDeployed(app)">
                          <template #icon><ExportIcon /></template>
                          查看部署
                        </a-button>
                      </div>
                    </div>
                  </div>
                  <div class="app-info">
                    <div class="user-row">
                      <a-avatar :src="app.userVO?.userAvatar" size="small" />
                      <span>{{ app.userVO?.userName || '匿名用户' }}</span>
                    </div>
                    <h3>{{ app.appName }}</h3>
                    <p>{{ formatTime(app.createTime) }}</p>
                  </div>
                </div>
              </a-col>
            </a-row>
            <a-empty v-else description="暂无精选案例" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { message, Empty } from 'ant-design-vue'
import { listMyApp, listFeaturedApp, addApp } from '@/api/appController'
import { useUserLoginStore } from '@/stores/UserLoginStore'

const router = useRouter()
const userLoginStore = useUserLoginStore()

const prompt = ref('')
const codeGenType = ref('html')
const creating = ref(false)
const myApps = ref<API.AppVO[]>([])
const featuredApps = ref<API.AppVO[]>([])

const recommendTopics = [
  {
    label: '博客网站',
    prompt: '创建一个个人博客网站，包含文章列表、详情页、关于页面',
    icon: 'BlogIcon',
  },
  {
    label: '待办清单',
    prompt: '创建一个待办事项管理应用，支持添加、删除、标记完成',
    icon: 'CheckCircleIcon',
  },
  {
    label: '天气查询',
    prompt: '创建一个天气查询应用，显示当前天气和未来几天预报',
    icon: 'CloudIcon',
  },
  { label: '图片画廊', prompt: '创建一个图片画廊应用，支持上传和浏览图片', icon: 'ImageIcon' },
]

const SparklesIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'icon-svg',
    },
    [
      h('path', {
        d: 'm12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3',
      }),
      h('path', { d: 'M5 3v4' }),
      h('path', { d: 'M19 17v4' }),
      h('path', { d: 'M3 5h4' }),
      h('path', { d: 'M17 19h4' }),
    ],
  )

const RocketIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'icon-svg',
    },
    [
      h('path', {
        d: 'M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09z',
      }),
      h('path', {
        d: 'm12 15-3-3a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.35 22.35 0 0 1-4 2z',
      }),
      h('path', { d: 'M9 12H4s.55-3.03 2-4c1.62-1.08 5 0 5 0' }),
      h('path', { d: 'M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5' }),
    ],
  )

const FolderOpenIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'icon-svg',
    },
    [
      h('path', {
        d: 'm6 14 1.45-2.9A2 2 0 0 1 9.24 10H20a2 2 0 0 1 1.94 2.5l-1.55 6a2 2 0 0 1-1.94 1.5H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h3.9a2 2 0 0 1 1.69.9l.81 1.2a2 2 0 0 0 1.67.9H18a2 2 0 0 1 2 2v2',
      }),
    ],
  )

const StarIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'currentColor',
      class: 'icon-svg',
    },
    [
      h('path', {
        d: 'M12 17.75l-6.172 3.245 1.179 -6.873 -5 -4.867 6.9 -1 3 -6.673 3 6.673 6.9 1 -5 4.867 1.179 6.873z',
      }),
    ],
  )

const AppstoreIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'icon-svg',
    },
    [
      h('rect', { x: '3', y: '3', width: '7', height: '7' }),
      h('rect', { x: '14', y: '3', width: '7', height: '7' }),
      h('rect', { x: '14', y: '14', width: '7', height: '7' }),
      h('rect', { x: '3', y: '14', width: '7', height: '7' }),
    ],
  )

const MessageIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'icon-svg',
    },
    [h('path', { d: 'M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z' })],
  )

const EyeIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'icon-svg',
    },
    [
      h('path', { d: 'M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z' }),
      h('circle', { cx: '12', cy: '12', r: '3' }),
    ],
  )

const ExportIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'icon-svg',
    },
    [
      h('path', { d: 'M12 3v12' }),
      h('path', { d: 'm5 8 7-7 7 7' }),
      h('path', { d: 'M21 16v-5a2 2 0 0 0-2-2h-5' }),
    ],
  )

const BlogIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'icon-svg',
    },
    [
      h('path', {
        d: 'M4 22h16a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v16a2 2 0 0 1-2 2Zm0 0a2 2 0 0 1-2-2v-9c0-1.1.9-2 2-2h2',
      }),
      h('path', { d: 'M18 14h-8' }),
      h('path', { d: 'M15 18h-5' }),
      h('path', { d: 'M10 6h8v4h-8V6Z' }),
    ],
  )

const CheckCircleIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'icon-svg',
    },
    [
      h('path', { d: 'M22 11.08V12a10 10 0 1 1-5.93-9.14' }),
      h('polyline', { points: '22 4 12 14.01 9 11.01' }),
    ],
  )

const CloudIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'icon-svg',
    },
    [
      h('path', { d: 'M17.5 19c0-3.037-2.463-5.5-5.5-5.5S6.5 15.963 6.5 19' }),
      h('path', { d: 'M20.4 16.11A6.5 6.5 0 0 0 15 12c-2.2 0-4.05.99-5.31 2.52' }),
    ],
  )

const ImageIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'icon-svg',
    },
    [
      h('rect', { x: '3', y: '3', width: '18', height: '18', rx: '2', ry: '2' }),
      h('circle', { cx: '8.5', cy: '8.5', r: '1.5' }),
      h('polyline', { points: '21 15 16 10 5 21' }),
    ],
  )

const selectTopic = (topicPrompt: string) => {
  prompt.value = topicPrompt
}

const handleCreateApp = async () => {
  if (!prompt.value.trim()) {
    message.warning('请输入提示词')
    return
  }

  if (!userLoginStore.loginUser.id) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: prompt.value,
      codeGenType: codeGenType.value,
    })
    if (res.code === 200 && res.data) {
      message.success('应用创建成功')
      const appId = String(res.data)
      router.push(`/chat/${appId}`)
    } else {
      message.error(res.message || '创建失败')
    }
  } catch (error) {
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

const goToChat = (appId: number) => {
  router.push(`/chat/${String(appId)}`)
}

const previewApp = (app: API.AppVO) => {
  message.info('预览功能开发中...')
}

const viewDeployed = (app: API.AppVO) => {
  if (app.deployKey) {
    window.open(`/static/${app.deployKey}`, '_blank')
  }
}

const formatTime = (time?: string) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

const loadMyApps = async () => {
  if (!userLoginStore.loginUser.id) return
  try {
    const res = await listMyApp({
      userId: userLoginStore.loginUser.id as number,
      pageNum: 1,
      pageSize: 10,
    })
    if (res.code === 200 && res.data?.records) {
      myApps.value = res.data.records
    }
  } catch (error) {
    console.error('加载我的作品失败', error)
  }
}

const loadFeaturedApps = async () => {
  try {
    const res = await listFeaturedApp({
      priority: 99,
      pageNum: 1,
      pageSize: 10,
    })
    if (res.code === 200 && res.data?.records) {
      featuredApps.value = res.data.records
    }
  } catch (error) {
    console.error('加载精选案例失败', error)
  }
}

onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})
</script>

<style scoped>
.home-view {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  padding-top: 80px;
}

/* 动态渐变背景 */
.bg-gradient {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  z-index: -1;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -200px;
  left: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: -150px;
  right: -100px;
  animation-delay: -7s;
}

.orb-3 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -14s;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.05);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.95);
  }
}

.main-content {
  max-width: 100%;
  padding: 40px 24px;
}

/* 对话输入区 */
.chat-section {
  text-align: center;
  padding-top: 80px;
  margin-bottom: 256px;
}

.chat-title h1 {
  font-size: 48px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 12px 0;
  background: linear-gradient(135deg, #fff 0%, #94a3b8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-logo {
  width: 48px;
  height: 48px;
  vertical-align: middle;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 32px 0;
}

.chat-input-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.input-box {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.input-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-main {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.input-icon {
  width: 24px;
  height: 24px;
  color: rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
  margin-top: 4px;
}

.custom-textarea {
  flex: 1;
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  color: #fff;
  font-size: 16px;
  line-height: 1.6;
  resize: vertical;
  min-height: 100px;
  max-height: 300px;
  overflow-y: auto;
  font-family: inherit;
  display: block;
  padding: 0;
}

.custom-textarea::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.input-footer-left {
  display: flex;
}

.input-footer-right {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.prompt-input :deep(.ant-input::placeholder) {
  color: rgba(255, 255, 255, 0.5) !important;
}

.prompt-input :deep(.ant-input-textarea-show-count::after) {
  color: rgba(255, 255, 255, 0.5) !important;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.type-select {
  width: 120px;
}

.type-select :deep(.ant-select-selector) {
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  color: #fff !important;
}

.type-select :deep(.ant-select-selection-item) {
  color: #fff !important;
}

.type-select :deep(.ant-select-arrow) {
  color: rgba(255, 255, 255, 0.5) !important;
}

.type-select :deep(.ant-select-dropdown) {
  background: rgba(30, 41, 59, 0.95) !important;
  backdrop-filter: blur(12px);
}

.type-select :deep(.ant-select-item) {
  color: rgba(255, 255, 255, 0.85) !important;
}

.type-select :deep(.ant-select-item-option-active) {
  background: rgba(255, 255, 255, 0.1) !important;
}

.type-select :deep(.ant-select-item-option-selected) {
  background: rgba(102, 126, 234, 0.3) !important;
}

.input-icon {
  color: rgba(255, 255, 255, 0.5);
}

.create-btn-circle {
  width: 44px;
  height: 44px;
  padding: 0;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.create-btn-circle:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.create-btn-circle:has(.arrow-up-icon.active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.arrow-up-icon {
  width: 20px;
  height: 20px;
  color: #9ca3af;
  transition: color 0.3s ease;
}

.arrow-up-icon.active {
  color: #fff;
}

.create-btn-circle:disabled {
  background: rgba(255, 255, 255, 0.1) !important;
}

/* 推荐主题 */
.recommend-topics {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
}

.recommend-topics .label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.topics {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.topic-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  transform: translateY(-2px);
}

.topic-btn :deep(.icon-svg) {
  width: 14px;
  height: 14px;
  margin-right: 4px;
}

/* 内容展示区 */
.content-section {
  margin-top: 80px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  padding: 24px;
  min-height: 400px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  width: 60%;
  margin: 0 auto;
  box-sizing: border-box;
}

.section-card :deep(.ant-divider) {
  border-color: rgba(255, 255, 255, 0.15);
  margin: 32px 0;
}

/* 统一卡片样式 */
.app-item {
  cursor: pointer;
}

.app-card {
  background: transparent;
  border: none;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.app-item:hover .app-card {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
}

.app-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 10;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-cover .default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-cover .default-cover :deep(.icon-svg) {
  width: 40px;
  height: 40px;
  color: rgba(255, 255, 255, 0.5);
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.65);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.app-card:hover .card-overlay {
  opacity: 1;
}

.app-info {
  padding: 12px 0 0 0;
}

.app-info h3 {
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  margin: 6px 0 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-info p {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
}

.app-info .user-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.app-info .user-row span {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin: 0;
}

.section-icon {
  width: 24px;
  height: 24px;
  color: #667eea;
}

.section-icon.featured {
  color: #f59e0b;
}

/* 内容区块 */
.content-block {
  margin-bottom: 8px;
}

.content-block:last-child {
  margin-bottom: 0;
}

.works-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.works-grid {
  max-width: 800px;
  margin: 0 auto;
}

.works-list {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.work-card {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.work-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.work-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 10;
  border-radius: 8px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.work-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.work-cover .default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.work-cover .default-cover :deep(.icon-svg) {
  width: 40px;
  height: 40px;
  color: rgba(255, 255, 255, 0.5);
}

.work-actions-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.work-card:hover .work-actions-overlay {
  opacity: 1;
}

.work-info {
  padding: 12px;
}

.work-info h3 {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.work-info .create-time {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
}

/* 精选案例 - 网格布局 */
.featured-works {
  min-height: auto;
  max-width: 900px;
  margin: 0 auto;
}

.featured-card {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s ease;
}

.featured-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.featured-cover {
  position: relative;
  aspect-ratio: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.featured-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.featured-cover .default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.featured-cover .default-cover :deep(.icon-svg) {
  width: 48px;
  height: 48px;
  color: rgba(255, 255, 255, 0.5);
}

.featured-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.featured-card:hover .featured-actions {
  opacity: 1;
}

.featured-info {
  padding: 16px;
}

.featured-info .user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.featured-info .user-name {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.featured-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.default-cover.square {
  aspect-ratio: 1;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-title h1 {
    font-size: 28px;
    flex-wrap: wrap;
  }

  .title-logo {
    width: 36px;
    height: 36px;
  }

  .subtitle {
    font-size: 16px;
  }

  .input-container {
    flex-direction: column;
  }

  .create-btn {
    width: 100%;
  }

  .app-card {
    flex-wrap: wrap;
  }

  .app-actions {
    width: 100%;
    justify-content: flex-start;
    margin-top: 12px;
    opacity: 1 !important;
  }
}

.icon-svg {
  width: 16px;
  height: 16px;
  vertical-align: middle;
}

/* Ant Design 组件暗色适配 */
.home-view :deep(.ant-empty-description) {
  color: rgba(255, 255, 255, 0.6);
}

.home-view :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.home-view :deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%) !important;
}

.home-view :deep(.ant-btn-default) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.home-view :deep(.ant-btn-default:hover) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.home-view :deep(.ant-avatar) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>
