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
            <div class="input-main">
              <SparklesIcon class="input-icon" />
              <textarea
                v-model="prompt"
                placeholder="描述你想要的应用..."
                class="custom-textarea"
                rows="3"
                @keydown.enter="handleCreateApp"
              ></textarea>
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
            <div class="responsive-grid" v-if="myApps.length > 0">
              <div v-for="app in myApps" :key="app.id" class="app-item">
                <div class="app-card">
                  <div class="app-cover">
                    <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
                    <div v-else class="default-cover">
                      <AppstoreIcon />
                    </div>
                    <div class="card-overlay">
                      <a-button type="primary" @click.stop="goToChat(app.id as any)">
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
            </div>
            <a-empty v-else description="暂无作品" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
            <div v-if="myApps.length > 0 && myApps.length < myAppsTotal" class="load-more-wrapper">
              <a-button
                type="primary"
                :loading="myAppsLoading"
                @click="loadMoreMyApps"
                class="load-more-btn"
              >
                加载更多
              </a-button>
            </div>
          </div>

          <!-- 精选案例 -->
          <div class="content-block">
            <div class="section-header">
              <StarIcon class="section-icon featured" />
              <h2>精选案例</h2>
            </div>
            <div class="responsive-grid" v-if="featuredApps.length > 0">
              <div v-for="app in featuredApps" :key="app.id" class="app-item">
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
            </div>
            <a-empty v-else description="暂无精选案例" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
            <div v-if="featuredApps.length > 0 && featuredApps.length < featuredAppsTotal" class="load-more-wrapper">
              <a-button
                type="primary"
                :loading="featuredAppsLoading"
                @click="loadMoreFeaturedApps"
                class="load-more-btn"
              >
                加载更多
              </a-button>
            </div>
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
const creating = ref(false)
const myApps = ref<API.AppVO[]>([])
const featuredApps = ref<API.AppVO[]>([])
const myAppsTotal = ref(0)
const featuredAppsTotal = ref(0)
const MY_APPS_PAGE_SIZE = 12
const FEATURED_APPS_PAGE_SIZE = 12
const myAppsPageNum = ref(1)
const featuredAppsPageNum = ref(1)
const myAppsLoading = ref(false)
const featuredAppsLoading = ref(false)

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
  router.push({ path: `/chat/${String(appId)}`, query: { isView: '1' } })
}

const previewApp = (app: API.AppVO) => {
  const codeGenType = app.codeGenType || 'html'
  let previewUrl = '';
  if(codeGenType === 'vue_project'){
    previewUrl = `http://localhost:8123/api/static/${codeGenType}_${app.id}/dist/index.html`
  }else{
    previewUrl = `http://localhost:8123/api/static/${codeGenType}_${app.id}/`
  }
  
  if (previewUrl) {
    window.open(previewUrl, '_blank')
  } else {
    message.warning('请先生成代码后在预览页面查看效果')
  }
}

const viewDeployed = (app: API.AppVO) => {
  if (app.deployKey) {
    window.open(`http://localhost/${app.deployKey}`, '_blank')
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

const loadMoreMyApps = () => {
  const nextPage = myAppsPageNum.value + 1
  myAppsPageNum.value = nextPage
  loadMyApps(nextPage, true)
}

const loadMoreFeaturedApps = () => {
  const nextPage = featuredAppsPageNum.value + 1
  featuredAppsPageNum.value = nextPage
  loadFeaturedApps(nextPage, true)
}

const loadMyApps = async (pageNum = 1, append = false) => {
  if (!userLoginStore.loginUser.id) return
  if (append) myAppsLoading.value = true
  try {
    const res = await listMyApp({
      userId: userLoginStore.loginUser.id as any,
      pageNum,
      pageSize: MY_APPS_PAGE_SIZE,
      sortField: 'createTime',
      sortOrder: 'descend',
    })
    if (res.code === 200 && res.data?.records) {
      if (append) {
        myApps.value = [...myApps.value, ...res.data.records]
      } else {
        myApps.value = res.data.records
      }
      myAppsTotal.value = res.data.totalRow || res.data.total || 0
    }
  } catch (error) {
    console.error('加载我的作品失败', error)
  } finally {
    if (append) myAppsLoading.value = false
  }
}

const loadFeaturedApps = async (pageNum = 1, append = false) => {
  if (append) featuredAppsLoading.value = true
  try {
    const res = await listFeaturedApp({
      priority: 99,
      pageNum,
      pageSize: FEATURED_APPS_PAGE_SIZE,
    })
    if (res.code === 200 && res.data?.records) {
      if (append) {
        featuredApps.value = [...featuredApps.value, ...res.data.records]
      } else {
        featuredApps.value = res.data.records
      }
      featuredAppsTotal.value = res.data.totalRow || res.data.total || 0
    }
  } catch (error) {
    console.error('加载精选案例失败', error)
  } finally {
    if (append) featuredAppsLoading.value = false
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
  background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 50%, #80deea 100%);
  z-index: -1;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
  top: -200px;
  left: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(120deg, #66ccff 0%, #99eeff 100%);
  bottom: -150px;
  right: -100px;
  animation-delay: -7s;
}

.orb-3 {
  width: 400px;
  height: 400px;
  background: linear-gradient(120deg, #4facfe 0%, #00f2fe 100%);
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
  color: #1a1a2e;
  margin: 0 0 12px 0;
  background: linear-gradient(135deg, #1a1a2e 0%, #2d3748 100%);
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
  box-shadow: 0 4px 12px rgba(102, 204, 255, 0.3);
}

.subtitle {
  font-size: 18px;
  color: #4a5568;
  margin: 0 0 32px 0;
}

.chat-input-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.input-box {
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(102, 204, 255, 0.3);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 8px 32px rgba(102, 204, 255, 0.15);
}

.input-main {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.input-icon {
  width: 24px;
  height: 24px;
  color: #66ccff;
  flex-shrink: 0;
  margin-top: 4px;
}

.custom-textarea {
  flex: 1;
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  color: #1a1a2e;
  font-size: 16px;
  line-height: 1.6;
  resize: vertical;
  min-height: 100px;
  max-height: 300px;
  overflow-y: auto;
  font-family: inherit;
  display: block;
  padding: 0;
  padding-right: 56px;
  box-sizing: border-box;
}

.custom-textarea::placeholder {
  color: #a0aec0;
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

.input-icon {
  color: #66ccff;
}

.create-btn-circle {
  position: absolute;
  right: 4px;
  bottom: 4px;
  width: 44px;
  height: 44px;
  padding: 0;
  border-radius: 50%;
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  flex-shrink: 0;
}

.create-btn-circle:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 204, 255, 0.4);
}

.create-btn-circle:has(.arrow-up-icon.active) {
  background: linear-gradient(120deg, #66ccff 0%, #99eeff 100%);
}

.arrow-up-icon {
  width: 20px;
  height: 20px;
  color: #a0aec0;
  transition: color 0.3s ease;
}

.arrow-up-icon.active {
  color: #fff;
}

.create-btn-circle:disabled {
  background: rgba(160, 174, 192, 0.3) !important;
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
  color: #4a5568;
  font-size: 14px;
}

.topics {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-btn {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(102, 204, 255, 0.3);
  color: #4a5568;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.topic-btn:hover {
  background: rgba(102, 204, 255, 0.2);
  color: #1a1a2e;
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
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(102, 204, 255, 0.2);
  border-radius: 20px;
  padding: 24px;
  min-height: 400px;
  box-shadow: 0 4px 20px rgba(102, 204, 255, 0.1);
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

/* 响应式卡片网格 */
.responsive-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

@media (max-width: 1400px) {
  .responsive-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1000px) {
  .responsive-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .responsive-grid {
    grid-template-columns: repeat(1, 1fr);
  }
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
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
  will-change: transform;
}

.app-item:hover .app-card {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(102, 204, 255, 0.25);
}

.app-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 10;
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
  overflow: hidden;
  will-change: opacity;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
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
  color: rgba(255, 255, 255, 0.7);
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  visibility: hidden;
  transition:
    opacity 0.15s ease,
    visibility 0.15s ease;
}

.app-card:hover .card-overlay {
  opacity: 1;
  visibility: visible;
}

.app-card:hover .card-overlay {
  opacity: 1;
  visibility: visible;
}

.app-info {
  padding: 12px 0 0 0;
}

.app-info h3 {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 6px 0 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-info p {
  font-size: 12px;
  color: #718096;
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
  color: #4a5568;
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
  color: #1a1a2e;
  margin: 0;
}

.section-icon {
  width: 24px;
  height: 24px;
  color: #66ccff;
}

.section-icon.featured {
  color: #84fab0;
}

/* 内容区块 */
.content-block {
  margin-bottom: 8px;
}

.content-block:last-child {
  margin-bottom: 0;
  margin-top: 40px;
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
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(102, 204, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.work-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 204, 255, 0.3);
}

.work-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 10;
  border-radius: 8px;
  overflow: hidden;
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
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
  color: rgba(255, 255, 255, 0.7);
}

.work-actions-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(102, 204, 255, 0.85);
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
  color: #1a1a2e;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.work-info .create-time {
  font-size: 13px;
  color: #718096;
  margin: 0;
}

/* 精选案例 - 网格布局 */
.featured-works {
  min-height: auto;
  max-width: 900px;
  margin: 0 auto;
}

.featured-card {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(102, 204, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
  will-change: transform;
}

.featured-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(102, 204, 255, 0.25);
}

.featured-cover {
  position: relative;
  aspect-ratio: 1;
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
  overflow: hidden;
  will-change: opacity;
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
  color: rgba(255, 255, 255, 0.7);
}

.featured-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  visibility: hidden;
  transition:
    opacity 0.15s ease,
    visibility 0.15s ease;
}

.featured-card:hover .featured-actions {
  opacity: 1;
  visibility: visible;
}

.featured-card:hover .featured-actions {
  opacity: 1;
  visibility: visible;
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
  color: #4a5568;
}

.featured-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
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

/* 加载更多按钮 */
.load-more-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.load-more-btn {
  min-width: 160px;
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

/* Ant Design 组件浅色适配 */
.home-view :deep(.ant-empty-description) {
  color: #4a5568;
}

.home-view :deep(.ant-btn-primary) {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(102, 204, 255, 0.5);
  box-shadow: 0 2px 8px rgba(102, 204, 255, 0.2);
  color: #1a1a2e;
  font-weight: 600;
}

.home-view :deep(.ant-btn-primary:hover) {
  background: #fff !important;
  border-color: #66ccff !important;
}

.home-view :deep(.ant-btn-primary:hover) {
  background: linear-gradient(120deg, #5bb8e6 0%, #7dccf2 100%) !important;
}

.home-view :deep(.ant-btn-default) {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(102, 204, 255, 0.4);
  color: #4a5568;
}

.home-view :deep(.ant-btn-default:hover) {
  background: rgba(102, 204, 255, 0.15);
  border-color: rgba(102, 204, 255, 0.6);
  color: #1a1a2e;
}

.home-view :deep(.ant-avatar) {
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
}
</style>
