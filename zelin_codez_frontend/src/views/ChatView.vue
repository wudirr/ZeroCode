<template>
  <div id="ChatView">
    <!-- 应用详情 Modal -->
    <div v-if="showAppDetailModal" class="modal-overlay" @click="closeAppDetailModal">
      <div class="modal-content" @click.stop>
        <h3>应用详情</h3>
        <div class="app-detail">
          <img
            :src="appDetail?.userVO?.userAvatar || '/default-avatar.png'"
            alt="Avatar"
            class="avatar"
          />
          <p><strong>创建人:</strong> {{ appDetail?.userVO?.userName }}</p>
          <p><strong>创建时间:</strong> {{ appDetail?.createTime }}</p>
        </div>
        <div class="modal-actions">
          <button @click="editApp" class="btn-primary">修改</button>
          <button @click="confirmDelete" class="btn-danger">删除</button>
        </div>
        <button @click="closeAppDetailModal" class="close-btn">×</button>
      </div>
    </div>

    <!-- 部署 Modal -->
    <div v-if="showDeployModal" class="modal-overlay" @click="closeDeployModal">
      <div class="modal-content deploy-modal" @click.stop>
        <div class="deploy-success-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        </div>
        <h3>部署成功</h3>
        <p class="deploy-label">访问地址</p>
        <div class="deploy-url-box">
          <input :value="deployUrl" readonly class="deploy-url" ref="deployUrlInput" />
          <button @click="copyDeployUrl" class="btn-copy">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path
                d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
              />
            </svg>
          </button>
        </div>
        <button @click="closeDeployModal" class="btn-primary btn-done">知道了</button>
      </div>
    </div>

    <!-- 删除确认 Modal -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click="cancelDelete">
      <div class="modal-content" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除这个应用吗？此操作不可撤销。</p>
        <div class="modal-actions">
          <button @click="cancelDelete" class="btn-secondary">取消</button>
          <button @click="deleteAppFunc" class="btn-danger">确认删除</button>
        </div>
      </div>
    </div>

    <div class="chat-container">
      <!-- 左边聊天区域 -->
      <div class="chat-section">
        <div class="chat-header">
          <button @click="goBack" class="back-btn">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M15 18l-6-6 6-6" />
            </svg>
            返回
          </button>
          <h2>{{ appDetail?.appName || 'AI 对话' }}</h2>
            <span v-if="appDetail?.codeGenType" class="type-tag" :class="'type-' + appDetail.codeGenType">
              {{ codeGenTypeLabel }}
            </span>
        </div>
        <div class="messages" ref="messagesContainer" @scroll="handleScroll">
          <button
            v-if="hasMoreHistory && !isLoadingHistory"
            class="load-more-btn"
            @click="loadMoreHistory"
          >
            加载更多
          </button>
          <div v-if="isLoadingHistory" class="loading-indicator">加载中...</div>
          <div v-for="message in messages" :key="message.id" :class="['message', message.sender]">
            <img
              v-if="message.sender === 'user'"
              :src="appDetail?.userVO?.userAvatar || '/default-avatar.png'"
              class="avatar"
            />
            <img v-else src="@/favicon.ico" class="avatar" />
            <div class="message-content">
              <!-- 思考内容面板 -->
              <div v-if="message.thinkingContent" class="thinking-panel" :class="{ expanded: message.thinkingExpanded }">
                <div class="thinking-panel-header" @click="message.thinkingExpanded = !message.thinkingExpanded">
                  <div class="thinking-panel-title">
                    <svg class="brain-icon" viewBox="0 0 24 24" fill="none" stroke="#66ccff" stroke-width="2">
                      <path d="M12 2a7 7 0 0 1 7 7c0 2.5-1.3 4.7-3.3 6.1L12 22l-3.7-6.9C6.3 13.7 5 11.5 5 9a7 7 0 0 1 7-7z"/>
                      <path d="M9 9h0M15 9h0M9 13h0M15 13h0"/>
                    </svg>
                    <span>AI 思考过程</span>
                  </div>
                  <div class="thinking-panel-status">
                    <span class="status-dot done"></span>
                    <span class="status-text">已思考</span>
                  </div>
                  <svg class="thinking-chevron" :class="{ rotated: message.thinkingExpanded }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="6 9 12 15 18 9"/>
                  </svg>
                </div>
                <div class="thinking-panel-body">
                  <div class="thinking-text">{{ message.thinkingContent }}</div>
                </div>
              </div>

              <p v-if="!message.isMarkdown">{{ message.content }}</p>
              <MarkdownRenderer v-if="message.isMarkdown" :content="message.content" />
              <!-- 流式输出间隔时的"持续处理"指示器 -->
              <div
                v-if="message.id === currentMarkdownId && isProcessing && !message.isThinking"
                class="processing-indicator"
              >
                <span class="processing-text">正在调用工具，请稍候...</span>
                <div class="dots">
                  <div class="dot"></div>
                  <div class="dot"></div>
                  <div class="dot"></div>
                </div>
              </div>
              <div v-if="message.isThinking" class="thinking">
                <span class="thinking-text">{{ thinkingText }}</span>
                <div class="dots">
                  <div class="dot"></div>
                  <div class="dot"></div>
                  <div class="dot"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <ElementInfoBar
          v-if="visualEditor.selectedElements.value.length > 0"
          :elements="visualEditor.selectedElements.value"
          @remove="visualEditor.removeElement"
          @clear="visualEditor.clearSelection"
        />
        <!-- 任务计划面板：固定在聊天区底部，极简风格 -->
        <div v-if="currentPlanSteps.length > 0" class="plan-panel-wrapper">
          <div class="plan-panel-header">
            <svg class="plan-icon" viewBox="0 0 24 24" fill="none" stroke="#66ccff" stroke-width="2">
              <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/>
            </svg>
            <span class="plan-panel-title">任务计划</span>
            <span class="plan-panel-summary">{{ currentPlanSummary }}</span>
          </div>
          <div class="plan-panel-body">
            <div v-for="(step, idx) in currentPlanSteps" :key="idx" class="plan-step" :class="'step-status-' + step.status">
              <span class="step-marker">
                <svg v-if="step.status === 'done'" class="step-icon" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2.5">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                <span v-else-if="step.status === 'in_progress'" class="step-spinner"></span>
                <span v-else class="step-pending"></span>
              </span>
              <span class="step-title" :class="{ 'step-done-text': step.status === 'done' }">{{ step.title }}</span>
              <span v-if="step.dependency" class="step-dependency">{{ step.dependency }}</span>
            </div>
          </div>
        </div>
        <div class="input-section">
          <input
            v-model="userInput"
            @keyup.enter="sendMessage"
            placeholder="输入您的消息..."
            class="message-input"
          />
          <EditModeButton
            :disabled="!previewUrl || isThinking"
            :is-edit-mode="visualEditor.isEditMode.value"
            @toggle="visualEditor.toggleEditMode"
          />
          <button @click="sendMessage" :disabled="!userInput.trim() || isThinking" class="send-btn">
            发送
          </button>
        </div>
      </div>

      <!-- 右边预览区域 -->
      <div class="code-section">
        <div class="code-header">
          <button @click="showAppDetail" class="btn-secondary" :disabled="!previewUrl || isThinking || isBuilding">
            <svg
              class="btn-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            应用详情
          </button>
          <button @click="openInNewWindow" class="btn-secondary" :disabled="!previewUrl || isThinking || isBuilding">
            <svg
              class="btn-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4v6h0m-3 8v6m-4-4h6"
              />
            </svg>
            新窗口
          </button>
          <button
            @click="deployAppAction"
            :disabled="!previewUrl || isThinking || isBuilding || deploying"
            class="btn-primary"
            :class="{ 'is-loading': deploying }"
          >
            <svg
              v-if="!deploying"
              class="btn-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M12 19l9-9m0 0l-9 9m9-9v6m-5.65-3.65l4.65 4.65M5 10.5a7.5 7.5 0 0115 0v7.5a7.5 7.5 0 01-15 0v-7.5z"
              />
            </svg>
            <svg v-else class="btn-icon spin-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            {{ deploying ? '部署中' : '部署' }}
          </button>
          <button
            @click="downloadCode"
            :disabled="!previewUrl || isThinking || downloading"
            class="btn-download"
            :class="{ 'is-loading': downloading }"
          >
            <svg
              v-if="!downloading"
              class="btn-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3"
              />
            </svg>
            <svg v-else class="btn-icon spin-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            {{ downloading ? '下载中' : '下载代码' }}
          </button>
        </div>
        <iframe
          v-if="messages.length > 0 && previewUrl"
          :key="'preview-' + previewVersion"
          :src="previewUrl"
          class="preview-iframe"
          :ref="visualEditor.setIframeRef"
        ></iframe>
        <!-- Vue 项目构建中动画 -->
        <div v-else-if="isBuilding" class="preview-empty building-state">
          <div class="building-icon-wrapper">
            <svg class="building-gear" viewBox="0 0 24 24" fill="none" stroke="#66ccff" stroke-width="1.5">
              <path d="M12 15a3 3 0 100-6 3 3 0 000 6z" />
              <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z" />
            </svg>
            <svg class="building-box" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z" />
              <polyline points="3.27 6.96 12 12.01 20.73 6.96" />
              <line x1="12" y1="22.08" x2="12" y2="12" />
            </svg>
          </div>
          <h3 class="building-title">项目构建中...</h3>
          <p class="building-hint">Vue 项目需要编译，请稍候</p>
          <div class="building-progress-bar">
            <div class="building-progress-fill"></div>
          </div>
          <div class="building-steps">
            <div class="building-step">
              <span class="step-dot active"></span>
              <span class="step-text">代码生成</span>
            </div>
            <div class="building-step-line active"></div>
            <div class="building-step">
              <span class="step-dot active pulse"></span>
              <span class="step-text active">项目构建</span>
            </div>
            <div class="building-step-line"></div>
            <div class="building-step">
              <span class="step-dot"></span>
              <span class="step-text">预览就绪</span>
            </div>
          </div>
        </div>
        <!-- 默认空状态 (robot) -->
        <div v-else class="preview-empty">
          <div class="robot-container">
            <svg class="robot-svg" viewBox="0 0 200 200">
              <circle cx="100" cy="90" r="50" fill="#e8f4fc" stroke="#66ccff" stroke-width="2" />
              <line x1="100" y1="40" x2="100" y2="55" stroke="#66ccff" stroke-width="2" />
              <circle cx="100" cy="35" r="6" fill="#ffd700" class="antenna-light" />
              <ellipse cx="80" cy="85" rx="8" ry="10" fill="#66ccff" class="eye" />
              <circle cx="80" cy="83" r="3" fill="#fff" class="eye-shine" />
              <ellipse cx="120" cy="85" rx="8" ry="10" fill="#66ccff" class="eye" />
              <circle cx="120" cy="83" r="3" fill="#fff" class="eye-shine" />
              <path
                d="M 80 105 Q 100 120 120 105"
                stroke="#66ccff"
                stroke-width="3"
                fill="none"
                class="mouth"
              />
              <rect
                x="70"
                y="145"
                width="60"
                height="40"
                rx="10"
                fill="#e8f4fc"
                stroke="#66ccff"
                stroke-width="2"
              />
              <rect
                x="80"
                y="150"
                width="40"
                height="20"
                rx="4"
                fill="#b3e5fc"
                class="robot-screen"
              />
              <rect
                x="50"
                y="150"
                width="15"
                height="30"
                rx="5"
                fill="#e8f4fc"
                stroke="#66ccff"
                stroke-width="2"
              />
              <rect
                x="135"
                y="150"
                width="15"
                height="30"
                rx="5"
                fill="#e8f4fc"
                stroke="#66ccff"
                stroke-width="2"
              />
            </svg>
          </div>
          <h3 class="empty-title">{{ statusText }}</h3>
          <p class="empty-hint">{{ statusHint }}</p>
          <div class="floating-icons">
            <svg
              class="float-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#66ccff"
              stroke-width="1.5"
            >
              <path d="M16 18l2-2v-5l-2-2-2 2v5l2 2m-10-4V8l-2-2-2 2v6l2 2m10-4l2 2 2-2-2-2" />
            </svg>
            <svg
              class="float-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#ffd700"
              stroke-width="1.5"
            >
              <path
                d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
              />
            </svg>
            <svg
              class="float-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#66ccff"
              stroke-width="1.5"
            >
              <path d="M9 17H7A5 5 0 017 7h2m0 0h2a5 5 0 010 10h-2m0 0h2M9 7h6m-3 10l3-3-3-3" />
            </svg>
          </div>
          <div v-if="isThinking" class="generating-dots">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { getApp, deployApp, deleteApp as deleteAppApi } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { useUserLoginStore } from '@/stores/UserLoginStore'
import { message } from 'ant-design-vue'
import { useVisualEditor } from '@/composables/useVisualEditor'
import EditModeButton from '@/components/VisualEditor/EditModeButton.vue'
import ElementInfoBar from '@/components/VisualEditor/ElementInfoBar.vue'

const route = useRoute()
const router = useRouter()
const userLoginStore = useUserLoginStore()

const visualEditor = useVisualEditor()

const BACKEND_BASE_URL = 'http://localhost:8123'
// iframe 预览使用相对路径，通过 Vite 代理转发，确保与主页面同源
const PREVIEW_BASE_URL = ''
const appId = ref<string>(route.params.appId as string)
const appDetail = ref<API.AppVO>({})

const codeGenTypeMap: Record<string, string> = {
  html: 'HTML',
  multi_file: '多文件',
  vue_project: 'Vue 项目',
}
const codeGenTypeLabel = computed(() => codeGenTypeMap[appDetail.value.codeGenType || ''] || '')
const userInput = ref<string>('')
const isThinking = ref<boolean>(false)
const messages = ref<any[]>([])
const messageId = ref<number>(0)
const currentMarkdownId = ref<number>(-1)
const previewUrl = ref<string>('')
const previewVersion = ref(0)
const pendingText = ref<string>('')
const isPageVisible = ref<boolean>(true)
const isAtBottom = ref(true)
const SCROLL_THRESHOLD = 50
const thinkingText = ref<string>('')
let thinkingInterval: any = null
let thinkingMsgIndex = 0
const thinkingMessages = [
  '思考中, 请稍候...',
  '正在分析您的需求...',
  '正在构思解决方案...',
  '正在编写代码...',
]

// 统一停止思考动画的辅助函数
const stopThinkingAnimation = () => {
  if (thinkingInterval) {
    clearInterval(thinkingInterval)
    thinkingInterval = null
  }
  thinkingText.value = ''
}

// 流式输出间隔指示器：内容输出暂停时显示"持续处理中"
const isProcessing = ref<boolean>(false)
let idleTimeout: any = null
const IDLE_THRESHOLD = 2500 // 2.5 秒无新内容判定为"处理中"

// 停止"持续处理"指示器
const stopProcessingIndicator = () => {
  if (idleTimeout) {
    clearTimeout(idleTimeout)
    idleTimeout = null
  }
  isProcessing.value = false
}

const hasMoreHistory = ref(true)
const isLoadingHistory = ref(false)
const lastCreateTime = ref<string>('')
const downloading = ref<boolean>(false)
const isBuilding = ref<boolean>(false)
const deploying = ref<boolean>(false)

// 任务计划面板（全局，固定在聊天区底部）
const currentPlanSteps = ref<PlanStep[]>([])
const currentPlanSummary = ref('')

const statusText = ref('开始对话吧，精彩即将呈现')
const statusHint = ref('AI 正在准备为您创作')

const showAppDetailModal = ref<boolean>(false)
const showDeployModal = ref<boolean>(false)
const showDeleteConfirm = ref<boolean>(false)
const deployUrl = ref<string>('')
const deployUrlInput = ref<HTMLInputElement | null>(null)

const messagesContainer = ref<HTMLDivElement | null>(null)

const goBack = () => {
  router.push('/')
}

const showAppDetail = () => {
  showAppDetailModal.value = true
}

const closeAppDetailModal = () => {
  showAppDetailModal.value = false
}

const editApp = () => {
  router.push(`/app/edit/${appDetail.value.id}`)
  closeAppDetailModal()
}

const confirmDelete = () => {
  showDeleteConfirm.value = true
  closeAppDetailModal()
}

const cancelDelete = () => {
  showDeleteConfirm.value = false
}

const deleteAppFunc = async () => {
  try {
    const res = await deleteAppApi({ id: appId.value as any })
    if (res.code === 200 && res.data) {
      message.success('删除成功')
      router.push('/')
    } else {
      message.error('删除失败，请重试')
    }
  } catch (error) {
    message.error('删除失败，请重试')
  }
  showDeleteConfirm.value = false
}

const deployAppAction = async () => {
  if (!previewUrl.value) {
    message.warning('请先生成代码')
    return
  }
  deploying.value = true
  try {
    const res = await deployApp({ appId: appId.value as any })
    const deployUrl_temp = String(res.data || '')
    deployUrl.value = deployUrl_temp.startsWith('http')
      ? deployUrl_temp
      : `${BACKEND_BASE_URL}${deployUrl_temp}`
    if (appDetail.value) {
      appDetail.value.deployKey = deployUrl_temp
    }
    showDeployModal.value = true
  } catch (error) {
    message.error('部署失败，请重试')
  } finally {
    deploying.value = false
  }
}

const closeDeployModal = () => {
  showDeployModal.value = false
}

const copyDeployUrl = () => {
  if (deployUrlInput.value) {
    deployUrlInput.value.select()
    document.execCommand('copy')
    message.success('地址已复制到剪贴板')
  }
}

const openInNewWindow = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  } else {
    message.warning('请先生成代码后在预览页面查看效果')
  }
}

const downloadCode = async () => {
  if (!previewUrl.value || isThinking.value || downloading.value) return

  downloading.value = true
  try {
    const url = `${BACKEND_BASE_URL}/api/app/download/${appId.value}`
    const response = await fetch(url, {
      credentials: 'include',
    })

    if (!response.ok) {
      message.error('下载失败，请重试')
      return
    }

    // 从响应头中提取文件名
    const contentDisposition = response.headers.get('Content-Disposition')
    let filename = `${appDetail.value.appName || 'code'}.zip`
    if (contentDisposition) {
      // 优先匹配 filename*=UTF-8''xxx 编码格式
      const utf8Match = contentDisposition.match(/filename\*=UTF-8''(.+)/i)
      if (utf8Match && utf8Match[1]) {
        filename = decodeURIComponent(utf8Match[1].replace(/"/g, ''))
      } else {
        // 回退匹配 filename="xxx" 格式
        const match = contentDisposition.match(/filename="?([^";\n]+)"?/i)
        if (match && match[1]) {
          filename = match[1]
        }
      }
    }

    const blob = await response.blob()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)

    message.success('下载成功')
  } catch (error) {
    message.error('下载失败，请重试')
  } finally {
    downloading.value = false
  }
}

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const checkIsAtBottom = () => {
  const container = messagesContainer.value
  if (!container) return true
  const distanceFromBottom = container.scrollHeight - container.scrollTop - container.clientHeight
  return distanceFromBottom <= SCROLL_THRESHOLD
}

const scrollToBottom = () => {
  const container = messagesContainer.value
  if (container && isAtBottom.value) {
    container.scrollTop = container.scrollHeight
  }
}

const handleScroll = () => {
  isAtBottom.value = checkIsAtBottom()

  if (
    messagesContainer.value &&
    messagesContainer.value.scrollTop < 50 &&
    hasMoreHistory.value &&
    !isLoadingHistory.value
  ) {
    loadMoreHistory()
  }
}

/** 计划步骤数据结构 */
interface PlanStep {
  status: 'done' | 'in_progress' | 'pending'
  title: string
  dependency?: string
}

/** 解析 updatePlan 的 result 字段，提取任务计划步骤 */
const parsePlanFromResult = (result: string): { steps: PlanStep[]; summary: string } => {
  // 处理所有可能的换行符表示形式（后端两层JSON序列化可能导致多重转义）
  let text = result
  // 情况C: \\n（两个字符的 \ 和一个字符的 \ + n）→ 先转为字面量 \n
  text = text.replace(/\\\\n/g, '\n')
  // 情况B: \n（字面量反斜杠+n）→ 转为真实换行
  text = text.replace(/\\n/g, '\n')
  // 处理 Windows 风格 \r\n
  text = text.replace(/\\r/g, '')

  const steps: PlanStep[] = []
  let summary = ''

  const lines = text.split('\n')
  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) continue

    // 匹配 [x] / [>] / [ ] 开头的步骤行
    const stepMatch = trimmed.match(/^\[(x|>| )\]\s+(.+)$/)
    if (stepMatch) {
      const marker = stepMatch[1]
      let status: PlanStep['status']
      if (marker === 'x') status = 'done'
      else if (marker === '>') status = 'in_progress'
      else status = 'pending'

      let title = stepMatch[2] || ''
      // 如果有 ##N 前缀，保留
      // 防止单步标题过长
      if (title.length > 200) {
        title = title.slice(0, 200) + '...'
      }

      steps.push({ status, title })
      continue
    }

    // 匹配"依赖计划"行，挂到上一步
    if (/^依赖/.test(trimmed) && steps.length > 0) {
      const lastStep = steps[steps.length - 1]
      if (lastStep) lastStep.dependency = trimmed
      continue
    }

    // 摘要行
    if (trimmed.includes('任务计划执行情况')) {
      summary = trimmed
    }
  }

  // 兜底解析：如果正则一条都没匹配到，按 ##N 切分
  if (steps.length === 0 && text.includes('##')) {
    const parts = text.split(/(?=##\d+)/)
    for (const part of parts) {
      const trimmed = part.trim()
      if (!trimmed) continue
      const stepMatch = trimmed.match(/^(##\d+)\s+(.+)$/s)
      if (stepMatch) {
        let title = (stepMatch[1] + ' ' + stepMatch[2]).trim()
        if (title.length > 200) {
          title = title.slice(0, 200) + '...'
        }
        steps.push({ status: 'pending' as PlanStep['status'], title })
      }
    }
  }

  return { steps, summary }
}

// 公共的发送消息给 AI 的函数
const sendPromptToAI = async (userMessage: string) => {
  // 每次发送前清空待处理文本
  pendingText.value = ''

  // 创建用户消息
  messages.value.push({
    id: messageId.value++,
    sender: 'user',
    content: userMessage,
  })

  // 创建 AI 消息确保有正确的 id
  const aiMsgId = messageId.value++
  messages.value.push({
    id: aiMsgId,
    sender: 'ai',
    isMarkdown: true,
    isThinking: true,
    content: '',
    thinkingContent: '',
    thinkingExpanded: false,
    hasPlan: false,
  })
  currentMarkdownId.value = aiMsgId

  isThinking.value = true
  userInput.value = ''

  // 启动思考文字轮播，每 4 秒切换，让用户在等待 AI 输出时明确感知"正在工作"
  thinkingMsgIndex = 0
  thinkingText.value = thinkingMessages[0]!
  thinkingInterval = setInterval(() => {
    thinkingMsgIndex = (thinkingMsgIndex + 1) % thinkingMessages.length
    thinkingText.value = thinkingMessages[thinkingMsgIndex]!
  }, 4000)

  await nextTick()
  scrollToBottom()

  const url = `${BACKEND_BASE_URL}/api/app/chat/gen/code?appId=${encodeURIComponent(appId.value)}&userMessage=${encodeURIComponent(userMessage)}`
  const source = new EventSource(url, { withCredentials: true })
  let isDone = false

  // 思考内容状态：累积 LLM 思考过程文本
  let accumulatedThinking = ''

  const closeSource = () => {
    if (source.readyState !== EventSource.CLOSED) {
      source.close()
    }
  }

  const appendText = async (text: string) => {
    if (!text) return
    if (isPageVisible.value) {
      const aiMessage = messages.value.find(
        (msg) =>
          msg.id === currentMarkdownId.value && msg.sender === 'ai' && msg.isMarkdown === true,
      )
      if (aiMessage) {
        if (aiMessage.isThinking) {
          aiMessage.isThinking = false
          // 收到首个 AI 输出，停止思考动画
          stopThinkingAnimation()
        }
        aiMessage.content += text
        // 重置空闲检测：正在输出，重新计时；超过阈值无新内容则显示"处理中"
        stopProcessingIndicator()
        idleTimeout = setTimeout(() => {
          isProcessing.value = true
        }, IDLE_THRESHOLD)
        await nextTick()
        scrollToBottom()
      }
    } else {
      pendingText.value += text
    }
  }

  source.onmessage = async (event) => {
    if (!event.data) return
    if (event.data === '[DONE]') {
      isDone = true
      closeSource()
      return
    }

    try {
      const parsed = JSON.parse(event.data)
      const rawD = parsed.d

      // 尝试二次解析 d 字段，判断是否为特殊事件
      if (rawD && typeof rawD === 'string') {
        try {
          const innerParsed = JSON.parse(rawD)

          // thinking_content：累积到思考面板
          if (innerParsed.type === 'thinking_content') {
            accumulatedThinking += innerParsed.data || ''
            const aiMessage = messages.value.find(
              (msg) => msg.id === currentMarkdownId.value && msg.sender === 'ai' && msg.isMarkdown === true,
            )
            if (aiMessage) {
              aiMessage.thinkingContent = accumulatedThinking
              aiMessage.thinkingExpanded = true
            }
            if (isPageVisible.value) {
              await nextTick()
              scrollToBottom()
            }
            return
          }

          // tool_request(updatePlan)：更新全局任务计划面板
          if (innerParsed.type === 'tool_request' && innerParsed.name === 'updatePlan') {
            const planData = parsePlanFromResult(innerParsed.result || '')
            currentPlanSteps.value = planData.steps
            currentPlanSummary.value = planData.summary
            // 标记当前 AI 消息有关联计划
            const aiMessage = messages.value.find(
              (msg) => msg.id === currentMarkdownId.value && msg.sender === 'ai',
            )
            if (aiMessage) {
              aiMessage.hasPlan = true
            }
            if (isPageVisible.value) {
              await nextTick()
              scrollToBottom()
            }
            return
          }
        } catch (_) {
          // d 不是 JSON 字符串，走普通流程
        }
      }

      // 普通消息处理：d 就是消息文本
      const text = parsed.d ?? parsed.content ?? event.data
      await appendText(String(text))
    } catch (err) {
      await appendText(event.data)
    }
  }

  source.addEventListener('done', async (event: MessageEvent) => {
    isDone = true
    closeSource()
    stopProcessingIndicator()
    // 清理思考内容状态
    accumulatedThinking = ''
    currentPlanSteps.value = []
    if (event.data && event.data !== '[]') {
      try {
        const parsed = JSON.parse(event.data)
        const text = parsed.d ?? parsed.content ?? event.data
        await appendText(String(text))
      } catch (err) {
        await appendText(event.data)
      }
    }
  })

  source.addEventListener('business-error', async (event: MessageEvent) => {
    isDone = true
    closeSource()
    stopThinkingAnimation()
    stopProcessingIndicator()
    // 清理思考内容状态
    accumulatedThinking = ''
    currentPlanSteps.value = []

    let errorMessage = '请求出错，请稍后重试。'
    try {
      const parsed = JSON.parse(event.data)
      errorMessage = parsed.message || errorMessage
    } catch (err) {
      errorMessage = event.data || errorMessage
    }

    // 复用当前"思考中"的 AI 消息气泡，替换为错误信息
    const aiMessage = messages.value.find(
      (msg) => msg.id === currentMarkdownId.value && msg.sender === 'ai',
    )
    if (aiMessage) {
      aiMessage.isThinking = false
      aiMessage.isMarkdown = false
      aiMessage.content = errorMessage
    }
  })

  source.onerror = () => {
    closeSource()
    if (!isDone) {
      // 关键：必须设置 isDone，否则 waitForDone 会死循环，sendPromptToAI 永久挂起
      isDone = true
      stopThinkingAnimation()
      stopProcessingIndicator()
      // 清理思考内容状态
      accumulatedThinking = ''
      currentPlanSteps.value = []
      const thinkingMsg = messages.value.find(
        (msg) => msg.isThinking && msg.id === currentMarkdownId.value,
      )
      if (thinkingMsg) {
        thinkingMsg.content = '生成代码时出错，请重试。'
        thinkingMsg.isThinking = false
      }
      isThinking.value = false
    }
  }

  const waitForDone = async () => {
    while (!isDone) {
      await sleep(100)
    }
    isThinking.value = false

    const aiMessage = messages.value.find(
      (msg) => msg.isMarkdown && msg.sender === 'ai' && msg.id === currentMarkdownId.value,
    )

    if (aiMessage && aiMessage.content) {
      const generationType = appDetail.value.codeGenType || 'html'
      console.log('生成类型:', generationType)
      const deployKey = `${generationType}_${appId.value}`
      if (generationType === 'vue_project') {
        // Vue 项目需要后端异步构建，先展示构建动画
        isBuilding.value = true
        aiMessage.content +=
          '\n\nVue 项目代码已生成，正在构建项目...'
        await sleep(25000)
        isBuilding.value = false
        previewUrl.value = `${PREVIEW_BASE_URL}/api/static/${deployKey}/dist/index.html`
        console.log(`预览地址: ${previewUrl.value}`)
      } else {
        previewUrl.value = `${PREVIEW_BASE_URL}/api/static/${deployKey}/`
      }
      previewVersion.value++
      aiMessage.content +=
        '\n\n代码已生成，现在为您显示预览页面。点击【部署】按钮可将应用部署到生产环境。'
    }

    // 清除思考动画
    stopThinkingAnimation()
    stopProcessingIndicator()

    currentMarkdownId.value = -1
  }

  await waitForDone()
}

// 自动发送初始提示词
const sendInitialPrompt = async (initPrompt: string) => {
  await sendPromptToAI(initPrompt)
}

// 用户发送消息
const sendMessage = async () => {
  if (!userInput.value.trim()) return
  const enhancedMessage = visualEditor.enhanceMessage(userInput.value.trim())
  visualEditor.cleanupAfterSend()
  await sendPromptToAI(enhancedMessage)
}

const loadChatHistory = async (loadMore = false) => {
  if (loadMore) {
    if (!hasMoreHistory.value || isLoadingHistory.value) return
    isLoadingHistory.value = true
  }

  const res = await listAppChatHistory({
    appId: appId.value as any,
    pageSize: 10,
    lastCreateTime: loadMore ? lastCreateTime.value : undefined,
  })

  if (res.code === 200 && res.data?.records) {
    let records = res.data.records.reverse()

    const historyMessages = records.map((item) => ({
      id: item.id,
      sender: item.messageType === 'user' ? 'user' : 'ai',
      content: item.message || '',
      isMarkdown: item.messageType !== 'user',
    }))

    if (loadMore) {
      messages.value = [...historyMessages, ...messages.value]
    } else {
      messages.value = historyMessages
    }

    if (records.length > 0) {
      const lastRecord = records[records.length - 1]
      lastCreateTime.value = lastRecord?.createTime || ''
    }

    hasMoreHistory.value = records.length === 10
  }

  isLoadingHistory.value = false
}

const loadMoreHistory = async () => {
  await loadChatHistory(true)
}

const updatePendingText = async () => {
  if (pendingText.value && isPageVisible.value) {
    const aiMessage = messages.value.find(
      (msg) => msg.isMarkdown && msg.sender === 'ai' && msg.id === currentMarkdownId.value,
    )
    if (aiMessage) {
      if (aiMessage.isThinking && pendingText.value) {
        aiMessage.isThinking = false
      }
      aiMessage.content += pendingText.value
      pendingText.value = ''
      await nextTick()
      scrollToBottom()
    }
  }
}

const handleVisibilityChange = () => {
  isPageVisible.value = !document.hidden
  if (isPageVisible.value) {
    updatePendingText()
  }
}

onMounted(async () => {
  document.documentElement.classList.add('hide-scroll')

  document.addEventListener('visibilitychange', handleVisibilityChange)

  try {
    const res = await getApp({ id: appId.value as any })
    appDetail.value = (res.data as API.AppVO) || {}

    await loadChatHistory()

    if (messages.value.length > 0) {
      const generationType = appDetail.value.codeGenType || 'html'
      const deployKey = `${generationType}_${appId.value}`;
      if (generationType === 'vue_project') {
        previewUrl.value = `${PREVIEW_BASE_URL}/api/static/${deployKey}/dist/index.html`
      } else {
        previewUrl.value = `${PREVIEW_BASE_URL}/api/static/${deployKey}/`
      }
      previewVersion.value++
    }

    if (messages.value.length === 0 && appDetail.value.initPrompt) {
      await sendInitialPrompt(appDetail.value.initPrompt as any)
    } else {
      await nextTick()
      scrollToBottom()
    }

    await nextTick()
  } catch (error) {
    message.error('获取应用详情失败')
  }
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  document.documentElement.classList.remove('hide-scroll')
  visualEditor.cleanup()
})

watch(isThinking, (newVal) => {
  if (newVal) {
    statusText.value = '正在为您创作...'
    statusHint.value = 'AI 正在生成精彩的代码'
  } else {
    statusText.value = '代码已生成'
    statusHint.value = '您可以在右侧查看生成的代码效果'
  }
})

watch(isBuilding, (newVal) => {
  if (newVal) {
    statusText.value = '正在构建项目...'
    statusHint.value = 'Vue 项目编译中，即将呈现'
  } else {
    statusText.value = '构建完成'
    statusHint.value = '您可以在右侧查看生成的代码效果'
  }
})

watch(previewVersion, () => {
  nextTick(() => {
    visualEditor.reInjectIfNeeded()
  })
})
</script>
<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;500;600;700&family=Fira+Sans:wght@300;400;500;600;700&display=swap');

#ChatView {
  font-family: 'Fira Sans', sans-serif;
  background-image:
    linear-gradient(60deg, #64b3f4 0%, #c2e59c 100%),
    radial-gradient(73% 147%, #eadfdf 59%, #ece2df 100%);
  background-blend-mode: screen;
  background-color: #f0f9ff;
  color: #1e293b;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-container {
  display: flex;
  flex: 1;
  overflow: hidden;
  padding: 16px;
  gap: 16px;
}

.chat-section {
  flex: 0 0 45%;
  display: flex;
  flex-direction: column;
  min-width: 0;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.5);
}

.back-btn {
  background: none;
  border: none;
  color: #1e293b;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 16px;
  transition: color 0.2s;
}

.back-btn:hover {
  color: #3b82f6;
}

.chat-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #1e293b;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.messages .message .avatar {
  width: 28px !important;
  height: 28px !important;
  border-radius: 50%;
  flex-shrink: 0;
  align-self: flex-start;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 100%;
  min-width: 0;
  overflow-x: visible;
}

.chat-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.type-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  letter-spacing: 0.3px;
  margin-left: 10px;
  vertical-align: middle;
}

.type-html {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #e65100;
}

.type-multi_file {
  background: linear-gradient(135deg, #e0f7ff, #b3e5fc);
  color: #0277bd;
}

.type-vue_project {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  color: #2e7d32;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.messages::-webkit-scrollbar {
  width: 6px;
}

.messages::-webkit-scrollbar-track {
  background: transparent;
}

.messages::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.messages::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 100%;
  min-width: 0;
  overflow-x: visible;
}

.messages .message .avatar {
  width: 32px !important;
  height: 32px !important;
  border-radius: 50%;
  flex-shrink: 0;
  align-self: flex-start;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 100%;
  min-width: 0;
  overflow-x: visible;
}

.message-content {
  padding: 2px 6px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  color: #1e293b;
  word-wrap: break-word;
  overflow-wrap: break-word;
  max-width: 80%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message.user .message-content {
  background: #bfdbfe;
}

/* 流式输出间隔的"持续处理"指示器 */
.processing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  margin-top: 4px;
}

.processing-text {
  color: #94a3b8;
  font-size: 12px;
}

.message.user {
  flex-direction: row-reverse;
}

.message.ai {
  align-self: flex-start;
}

.message.ai .message-content {
  background: rgba(255, 255, 255, 0.8);
  padding: 0;
  border-radius: 0;
  box-shadow: none;
  /* 固定宽度而非 max-width，消除流式输出时气泡宽度左右跳变 */
  width: 100%;
  max-width: 100%;
}

.message-content :deep(pre),
.message-content :deep(pre code) {
  max-width: 100%;
  overflow-x: auto;
  white-space: pre;
}

.thinking {
  display: flex;
  flex-direction: column-reverse;
  align-items: center;
  gap: 8px;
}

.thinking-text {
  color: #94a3b8;
  font-size: 12px;
}

.dots {
  display: flex;
  gap: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  background: #3b82f6;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0);
  }

  40% {
    transform: scale(1);
  }
}

@keyframes fadeInOut {
  0%,
  100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

.input-section {
  display: flex;
  padding: 16px;
  border-top: 1px solid #e2e8f0;
  gap: 8px;
  background: rgba(255, 255, 255, 0.5);
}

.message-input {
  flex: 1;
  padding: 12px;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  color: #1e293b;
  font-family: 'Fira Sans', sans-serif;
}

.message-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.send-btn {
  padding: 12px 16px;
  background: linear-gradient(60deg, #64b3f4 0%, #22c55e 100%);
  color: #fff;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: linear-gradient(60deg, #3b82f6 0%, #16a34a 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.send-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}

.load-more-btn {
  align-self: center;
  padding: 8px 16px;
  margin-bottom: 16px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #cbd5e1;
  border-radius: 20px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.load-more-btn:hover {
  background: #f1f5f9;
  color: #3b82f6;
}

.loading-indicator {
  text-align: center;
  padding: 12px;
  color: #94a3b8;
  font-size: 14px;
}

.code-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  position: relative;
  z-index: 1;
  height: 100%;
  overflow: hidden;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.code-header {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px 16px 0 0;
}

.btn-primary,
.btn-secondary,
.btn-danger,
.btn-download {
  padding: 6px 14px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-family: 'Fira Sans', sans-serif;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
}

.btn-icon {
  width: 14px;
  height: 14px;
}

.btn-primary {
  background: linear-gradient(60deg, #64b3f4 0%, #22c55e 100%);
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.btn-primary:disabled {
  background: #cbd5e1;
  color: #94a3b8;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-primary.is-loading {
  background: linear-gradient(60deg, #8cb8d8 0%, #6aa88a 100%);
  color: rgba(255, 255, 255, 0.8);
  cursor: wait;
}

.btn-download {
  background: linear-gradient(135deg, #66ccff 0%, #4fb3ff 100%);
  color: #fff;
}

.btn-download:hover:not(:disabled) {
  background: linear-gradient(135deg, #4fb3ff 0%, #3399ff 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 204, 255, 0.4);
}

.btn-download:disabled {
  background: #cbd5e1;
  color: #94a3b8;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-download.is-loading {
  background: linear-gradient(135deg, #94c8e8 0%, #8ab8d8 100%);
  color: rgba(255, 255, 255, 0.8);
  cursor: wait;
}

.spin-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.btn-secondary {
  background: #e2e8f0;
  color: #475569;
}

.btn-secondary:hover:not(:disabled) {
  background: #cbd5e1;
}

.btn-secondary:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}

.btn-danger {
  background: #fecaca;
  color: #dc2626;
}

.btn-danger:hover {
  background: #fca5a5;
}

.preview-iframe {
  flex: 1;
  border: none;
  width: 100%;
  height: 100%;
  min-height: 0;
  background: #fff;
  position: relative;
  z-index: 1;
  overflow: auto;
  border-radius: 0 0 16px 16px;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #f1f5f9;
}

.preview-iframe::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.preview-iframe::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.preview-iframe::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.preview-iframe::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.preview-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8fbfd 0%, #e8f4fc 100%);
  padding: 40px;
}

.robot-container {
  margin-bottom: 30px;
}

.robot-svg {
  width: 160px;
  height: 160px;
  animation: breathe 3s ease-in-out infinite;
}

@keyframes breathe {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.eye {
  animation: blink 4s ease-in-out infinite;
  transform-origin: center;
}

@keyframes blink {
  0%,
  45%,
  55%,
  100% {
    transform: scaleY(1);
  }
  50% {
    transform: scaleY(0.1);
  }
}

.antenna-light {
  animation: glow 1.5s ease-in-out infinite;
}

@keyframes glow {
  0%,
  100% {
    opacity: 1;
    filter: drop-shadow(0 0 5px #ffd700);
  }
  50% {
    opacity: 0.6;
    filter: drop-shadow(0 0 12px #ffd700);
  }
}

.robot-screen {
  animation: screenFlicker 2s ease-in-out infinite;
}

@keyframes screenFlicker {
  0%,
  100% {
    fill: #b3e5fc;
  }
  50% {
    fill: #e0f7ff;
  }
}

.mouth {
  animation: smile 3s ease-in-out infinite;
}

@keyframes smile {
  0%,
  100% {
    d: path('M 80 105 Q 100 115 120 105');
  }
  50% {
    d: path('M 80 105 Q 100 125 120 105');
  }
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 8px;
}

.empty-hint {
  font-size: 14px;
  color: #718096;
  margin: 0;
}

.floating-icons {
  display: flex;
  gap: 24px;
  margin-top: 30px;
}

.float-icon {
  width: 28px;
  height: 28px;
  animation: floatUp 3s ease-in-out infinite;
}

.float-icon:nth-child(1) {
  animation-delay: 0s;
}
.float-icon:nth-child(2) {
  animation-delay: 0.5s;
}
.float-icon:nth-child(3) {
  animation-delay: 1s;
}

@keyframes floatUp {
  0%,
  100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-15px);
    opacity: 1;
  }
}

.generating-dots {
  display: flex;
  gap: 6px;
  margin-top: 24px;
}

.generating-dots span {
  width: 8px;
  height: 8px;
  background: #66ccff;
  border-radius: 50%;
  animation: dotBounce 1.4s ease-in-out infinite;
}

.generating-dots span:nth-child(1) {
  animation-delay: -0.32s;
}
.generating-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes dotBounce {
  0%,
  80%,
  100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 构建动画样式 */
.building-state {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 50%, #f0fdf4 100%);
}

.building-icon-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  margin-bottom: 28px;
}

.building-gear {
  position: absolute;
  top: 0;
  right: 0;
  width: 44px;
  height: 44px;
  animation: spin 3s linear infinite;
}

.building-box {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 48px;
  height: 48px;
  color: #475569;
  animation: boxPulse 2s ease-in-out infinite;
}

.building-title {
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
}

.building-hint {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 32px;
}

.building-progress-bar {
  width: 240px;
  height: 6px;
  background: rgba(102, 204, 255, 0.2);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 32px;
}

.building-progress-fill {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, #66ccff 0%, #22c55e 100%);
  animation: progressFill 24.5s ease-in-out forwards;
}

.building-steps {
  display: flex;
  align-items: center;
  gap: 0;
}

.building-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #cbd5e1;
  transition: all 0.3s ease;
}

.step-dot.active {
  background: linear-gradient(135deg, #66ccff, #22c55e);
  box-shadow: 0 0 8px rgba(102, 204, 255, 0.4);
}

.step-dot.pulse {
  animation: dotPulse 1.5s ease-in-out infinite;
}

.step-text {
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
  font-weight: 500;
}

.step-text.active {
  color: #1e293b;
  font-weight: 600;
}

.building-step-line {
  width: 40px;
  height: 2px;
  background: #cbd5e1;
  margin: 0 8px;
  margin-bottom: 24px;
  border-radius: 1px;
  transition: background 0.3s ease;
}

.building-step-line.active {
  background: linear-gradient(90deg, #66ccff, #22c55e);
}

@keyframes progressFill {
  0% {
    width: 0%;
  }
  20% {
    width: 25%;
  }
  50% {
    width: 55%;
  }
  80% {
    width: 80%;
  }
  100% {
    width: 100%;
  }
}

@keyframes boxPulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.08);
    opacity: 0.85;
  }
}

@keyframes dotPulse {
  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 0 8px rgba(102, 204, 255, 0.4);
  }
  50% {
    transform: scale(1.3);
    box-shadow: 0 0 16px rgba(102, 204, 255, 0.6);
  }
}

@media (prefers-reduced-motion: reduce) {
  .building-gear,
  .building-box,
  .building-progress-fill,
  .step-dot.pulse {
    animation: none;
  }
  .building-progress-fill {
    width: 80%;
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  color: #1e293b;
  padding: 24px;
  border-radius: 16px;
  max-width: 400px;
  width: 90%;
  position: relative;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.deploy-modal {
  text-align: center;
  padding: 32px 24px;
}

.deploy-success-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #64b3f4 0%, #22c55e 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: popIn 0.4s ease-out;
}

.deploy-success-icon svg {
  width: 32px;
  height: 32px;
  color: white;
}

@keyframes popIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.deploy-modal h3 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
}

.deploy-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
  text-align: left;
}

.deploy-url-box {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.deploy-url {
  flex: 1;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  color: #1e293b;
  font-size: 14px;
}

.btn-copy {
  padding: 12px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #64b3f4 0%, #22c55e 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-copy svg {
  width: 18px;
  height: 18px;
}

.btn-copy:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.btn-done {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 500;
  text-align: center;
}

.modal-content h3 {
  margin-top: 0;
  font-size: 24px;
  font-weight: 500;
  color: #1e293b;
}

.app-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin: 16px 0;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.modal-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 16px;
}

.close-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  background: none;
  border: none;
  color: #64748b;
  font-size: 24px;
  cursor: pointer;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #1e293b;
}

.deploy-url {
  width: 100%;
  padding: 8px;
  margin: 8px 0;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #fff;
  color: #1e293b;
}

.deploy-url:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 思考内容面板样式 */
.thinking-panel {
  margin-bottom: 12px;
  border: 1px solid rgba(102, 204, 255, 0.3);
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(240, 249, 255, 0.8) 0%, rgba(230, 244, 252, 0.6) 100%);
  backdrop-filter: blur(8px);
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 204, 255, 0.15);
}

.thinking-panel:hover {
  box-shadow: 0 4px 12px rgba(102, 204, 255, 0.25);
  border-color: rgba(102, 204, 255, 0.5);
}

.thinking-panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  user-select: none;
  transition: background 0.2s ease;
}

.thinking-panel-header:hover {
  background: rgba(102, 204, 255, 0.1);
}

.thinking-panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  flex: 1;
  letter-spacing: 0.3px;
}

.brain-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.thinking-panel-status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.status-dot.thinking {
  background: #66ccff;
  box-shadow: 0 0 8px rgba(102, 204, 255, 0.6);
  animation: statusPulse 1.5s ease-in-out infinite;
}

.status-dot.done {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.4);
}

.status-text {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.thinking-chevron {
  width: 16px;
  height: 16px;
  transition: transform 0.3s ease;
  color: #94a3b8;
  flex-shrink: 0;
}

.thinking-chevron.rotated {
  transform: rotate(180deg);
}

.thinking-panel-body {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.thinking-panel.expanded .thinking-panel-body {
  max-height: 600px;
  overflow-y: auto;
}

.thinking-text {
  padding: 0 16px 16px;
  font-size: 13px;
  line-height: 1.7;
  color: #475569;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'Fira Code', 'Consolas', monospace;
  border-top: 1px solid rgba(102, 204, 255, 0.2);
  margin-top: 0;
  padding-top: 12px;
}

.thinking-panel.expanded .thinking-text {
  border-top: 1px solid rgba(102, 204, 255, 0.3);
}

/* 思考内容面板的滚动条样式 */
.thinking-panel-body::-webkit-scrollbar {
  width: 6px;
}

.thinking-panel-body::-webkit-scrollbar-track {
  background: rgba(102, 204, 255, 0.1);
  border-radius: 3px;
}

.thinking-panel-body::-webkit-scrollbar-thumb {
  background: rgba(102, 204, 255, 0.4);
  border-radius: 3px;
}

.thinking-panel-body::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 204, 255, 0.6);
}

@keyframes statusPulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}

/* 任务计划面板样式（极简） */

.plan-panel-wrapper {
  flex-shrink: 0;
  padding: 4px 16px 6px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.plan-panel-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 2px 0 4px;
}

.plan-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.plan-panel-title {
  font-size: 11px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.plan-panel-summary {
  font-size: 10px;
  color: #94a3b8;
  margin-left: auto;
}

.plan-panel-body {
  /* 纯块级流，子元素自然堆叠，物理上不可重叠 */
}

.plan-step {
  display: block;
  padding: 5px 0;
  min-height: 22px;
  line-height: 1.6;
}

.step-marker {
  display: inline-block;
  vertical-align: middle;
  width: 16px;
  height: 16px;
  margin-right: 8px;
}

.step-icon {
  display: inline-block;
  vertical-align: middle;
  width: 14px;
  height: 14px;
  animation: checkPop 0.4s ease-out;
}

.step-spinner {
  display: inline-block;
  vertical-align: middle;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #66ccff, #3b82f6);
  animation: planPulse 1.8s ease-in-out infinite;
  position: relative;
}

.step-spinner::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff;
  animation: spinnerCore 1.8s ease-in-out infinite;
}

.step-pending {
  display: inline-block;
  vertical-align: middle;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1.5px solid #cbd5e1;
  background: transparent;
}

.step-title {
  display: inline;
  vertical-align: middle;
  font-size: 12px;
  color: #475569;
  word-break: break-word;
  overflow-wrap: break-word;
}

.step-done-text {
  color: #94a3b8;
  text-decoration: line-through;
  text-decoration-color: rgba(148, 163, 184, 0.35);
}

.step-dependency {
  display: inline;
  font-size: 10px;
  color: #94a3b8;
  padding-left: 6px;
  margin-left: 4px;
  border-left: 1px solid rgba(102, 204, 255, 0.25);
}

@keyframes checkPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.2); }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes spinnerCore {
  0%, 100% { transform: scale(0.6); }
  50% { transform: scale(1); }
}

@keyframes planPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(102, 204, 255, 0.4); }
  50% { box-shadow: 0 0 0 3px rgba(102, 204, 255, 0); }
}

@media (max-width: 768px) {
  .thinking-panel-title { font-size: 13px; }
  .status-text { font-size: 11px; }
  .thinking-text { font-size: 12px; padding: 0 12px 12px; padding-top: 10px; }
  .plan-panel-title { font-size: 10px; }
  .plan-panel-summary { font-size: 9px; }
  .step-title { font-size: 11px; }
}
</style>

<style global>
/* 仅在聊天页面隐藏滚动条 */
html.hide-scroll {
  overflow: hidden !important;
}
html.hide-scroll body {
  overflow: hidden !important;
}
</style>
