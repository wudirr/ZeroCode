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
        </div>
        <div class="messages" ref="messagesContainer" @scroll="handleScroll">
          <div v-for="message in messages" :key="message.id" :class="['message', message.sender]">
            <img
              v-if="message.sender === 'user'"
              :src="appDetail?.userVO?.userAvatar || '/default-avatar.png'"
              class="avatar"
            />
            <img v-else src="@/favicon.ico" class="avatar" />
            <div class="message-content">
              <p v-if="!message.isMarkdown">{{ message.content }}</p>
              <MarkdownRenderer v-if="message.isMarkdown" :content="message.content" />
              <div v-if="message.isThinking" class="thinking">
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
              </div>
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
          <button @click="sendMessage" :disabled="!userInput.trim() || isThinking" class="send-btn">
            发送
          </button>
        </div>
      </div>

      <!-- 右边预览区域 -->
      <div class="code-section">
        <div class="code-header">
          <button @click="showAppDetail" class="btn-secondary">
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
          <button @click="openInNewWindow" class="btn-secondary">
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
          <button @click="deployAppAction" class="btn-primary">
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
                d="M12 19l9-9m0 0l-9 9m9-9v6m-5.65-3.65l4.65 4.65M5 10.5a7.5 7.5 0 0115 0v7.5a7.5 7.5 0 01-15 0v-7.5z"
              />
            </svg>
            部署
          </button>
        </div>
        <iframe
          v-if="previewUrl"
          :key="'preview-' + previewVersion"
          :src="previewUrl"
          class="preview-iframe"
        ></iframe>
        <div v-else class="no-preview">等待生成预览...</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { getApp, deployApp, deleteApp as deleteAppApi } from '@/api/appController'
import { useUserLoginStore } from '@/stores/UserLoginStore'
import { message } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const userLoginStore = useUserLoginStore()

const isView = route.query.isView === '1'

const BACKEND_BASE_URL = 'http://localhost:8123'
const appId = ref<string>(route.params.appId as string)
const appDetail = ref<API.AppVO>({})
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
  message.info('修改功能正在开发中...')
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
  })
  currentMarkdownId.value = aiMsgId

  isThinking.value = true
  userInput.value = ''

  await nextTick()
  scrollToBottom()

  const url = `${BACKEND_BASE_URL}/api/app/chat/gen/code?appId=${encodeURIComponent(appId.value)}&userMessage=${encodeURIComponent(userMessage)}`
  const source = new EventSource(url, { withCredentials: true })
  let isDone = false

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
        }
        aiMessage.content += text
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
      const text = parsed.d ?? parsed.content ?? event.data
      await appendText(String(text))
    } catch (err) {
      await appendText(event.data)
    }
  }

  source.addEventListener('done', async (event: MessageEvent) => {
    isDone = true
    closeSource()
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

  source.onerror = () => {
    closeSource()
    if (!isDone) {
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
      const deployKey = `${generationType}_${appId.value}`
      previewUrl.value = `${BACKEND_BASE_URL}/api/static/${deployKey}/`
      previewVersion.value++
      aiMessage.content +=
        '\n\n代码已生成，现在为您显示预览页面。点击【部署】按钮可将应用部署到生产环境。'
    }

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
  await sendPromptToAI(userInput.value.trim())
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
  // 通过给 html 添加类来隐藏滚动条，避免直接操作 style
  document.documentElement.classList.add('hide-scroll')

  document.addEventListener('visibilitychange', handleVisibilityChange)
  try {
    const res = await getApp({ id: appId.value as any })
    appDetail.value = (res.data as API.AppVO) || {}

    if (isView) {
      // 查看模式：直接显示预览页面，不自动发送
      // deployKey 可能不存在，需要根据 generationType 和 appId 构建
      const generationType = appDetail.value.codeGenType || 'html'
      const deployKey = `${generationType}_${appId.value}`
      previewUrl.value = `${BACKEND_BASE_URL}/api/static/${deployKey}/`
      previewVersion.value++

      // 显示欢迎消息
      messages.value.push({
        id: messageId.value++,
        sender: 'ai',
        isMarkdown: true,
        content: `欢迎回来！这是您之前创建的应用。点击预览区域右上角的【新窗口】可在新页面查看。`,
      })
    } else {
      // 新建模式：自动发送初始提示词给 AI
      await sendInitialPrompt(appDetail.value.initPrompt as any)
    }

    await nextTick()
  } catch (error) {
    message.error('获取应用详情失败')
  }
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  // 恢复滚动条
  document.documentElement.classList.remove('hide-scroll')
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
}

.message-content :deep(pre),
.message-content :deep(pre code) {
  max-width: 100%;
  overflow-x: auto;
  white-space: pre;
}

.thinking {
  display: flex;
  gap: 4px;
  align-items: center;
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
.btn-danger {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
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

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.btn-secondary {
  background: #e2e8f0;
  color: #475569;
}

.btn-secondary:hover {
  background: #cbd5e1;
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

.no-preview {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 18px;
  background: rgba(255, 255, 255, 0.5);
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
