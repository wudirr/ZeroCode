<template>
  <div class="app-manage-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <div class="content">
      <!-- 标题区域 -->
      <div class="header">
        <h1 class="title">
          <RocketOutlined class="title-icon" />
          应用管理
        </h1>
        <p class="subtitle">管理系统中的应用</p>
      </div>

      <!-- 搜索区域 -->
      <div class="search-section">
        <input v-model="searchParams.appName" type="text" placeholder="应用名称" class="search-input-inline"
          @keyup.enter="handleSearch" />
        <div class="custom-select" :class="{ 'is-open': openDropdown === 'codeGenType' }">
          <div class="select-trigger" :class="{ 'has-value': searchParams.codeGenType }"
            @click="toggleDropdown('codeGenType')">
            <span class="select-value">{{
              getLabel('codeGenType', searchParams.codeGenType) || '代码类型'
              }}</span>
            <span class="select-arrow"></span>
          </div>
          <div class="select-dropdown">
            <div class="select-option" :class="{ 'is-selected': searchParams.codeGenType === 'html' }"
              @click="selectOption('codeGenType', 'html')">
              HTML
            </div>
            <div class="select-option" :class="{ 'is-selected': searchParams.codeGenType === 'multi_file' }"
              @click="selectOption('codeGenType', 'multi_file')">
              多文件
            </div>
          </div>
        </div>
        <div class="custom-select" :class="{ 'is-open': openDropdown === 'priority' }">
          <div class="select-trigger" :class="{ 'has-value': searchParams.priority === 0 || searchParams.priority }"
            @click="toggleDropdown('priority')">
            <span class="select-value">{{
              getLabel('priority', searchParams.priority) || '作品等级'
              }}</span>
            <span class="select-arrow"></span>
          </div>
          <div class="select-dropdown">
            <div class="select-option" :class="{ 'is-selected': searchParams.priority === 0 }"
              @click="selectOption('priority', 0)">
              普通
            </div>
            <div class="select-option" :class="{ 'is-selected': searchParams.priority === 99 }"
              @click="selectOption('priority', 99)">
              精选
            </div>
          </div>
        </div>
        <div class="custom-select" :class="{ 'is-open': openDropdown === 'deployStatus' }">
          <div class="select-trigger" :class="{ 'has-value': deployStatus }" @click="toggleDropdown('deployStatus')">
            <span class="select-value">{{
              getLabel('deployStatus', deployStatus) || '部署状态'
              }}</span>
            <span class="select-arrow"></span>
          </div>
          <div class="select-dropdown">
            <div class="select-option" :class="{ 'is-selected': deployStatus === 'deployed' }"
              @click="selectOption('deployStatus', 'deployed')">
              已部署
            </div>
            <div class="select-option" :class="{ 'is-selected': deployStatus === 'not_deployed' }"
              @click="selectOption('deployStatus', 'not_deployed')">
              未部署
            </div>
          </div>
        </div>
        <input v-model="searchParams.userName" type="text" placeholder="创建者" class="search-input-inline"
          @keyup.enter="handleSearch" />
        <button class="btn-search" @click="handleSearch">搜索</button>
        <button class="btn-search" @click="handleReset">重置</button>
      </div>

      <!-- 表格区域 -->
      <div class="table-section">
        <CustomTable :columns="columns" :data="data" :total="total" :page-num="params.pageNum"
          :page-size="params.pageSize" @change="onChange">
          <template #cover="{ row }">
            <img v-if="row.cover" :src="row.cover" class="cover-img" />
            <div v-else class="cover-placeholder">
              <FileImageOutlined />
            </div>
          </template>
          <template #codeGenType="{ row }">
            <span class="tag" :class="row.codeGenType === 'multi_file' ? 'tag-multi' : row.codeGenType === 'html' ? 'tag-html' : row.codeGenType === 'vue_project' ? 'tag-vue' : 'tag-html'">
              <FolderOutlined v-if="row.codeGenType === 'multi_file'" />
              <FileTextOutlined v-else />
              {{ row.codeGenType === 'multi_file' ? '多文件' : row.codeGenType === 'html' ? 'HTML' : row.codeGenType === 'vue_project' ? 'Vue' : 'HTML' }}
            </span>
          </template>
          <template #priority="{ row }">
            <span class="priority-tag" :class="row.priority === 99 ? 'priority-top' : 'priority-normal'">
              {{ PriorityEnum[row.priority] || '普通' }}
            </span>
          </template>
          <template #deployKey="{ row }">
            <span class="tag nowrap" :class="row.deployKey ? 'tag-deployed' : 'tag-not-deployed'">
              <CheckCircleOutlined v-if="row.deployKey" />
              <ClockCircleOutlined v-else />
              {{ row.deployKey ? '已部署' : '未部署' }}
            </span>
          </template>
          <template #userVO="{ row }">
            <span class="user-name">{{ row.userVO?.userName || '-' }}</span>
          </template>
          <template #createTime="{ row }">
            <span class="time-text">{{ formatTime(row.createTime) }}</span>
          </template>
          <template #action="{ row }">
            <div class="action-buttons">
              <button class="action-btn preview-btn" @click.stop="previewApp(row)">
                <EyeOutlined /> 预览
              </button>
              <button class="action-btn featured-btn" :class="{ 'is-featured': row.priority === 99 }"
                @click.stop="toggleFeatured(row)">
                <StarOutlined />
                {{ row.priority === 99 ? '取消精选' : '精选' }}
              </button>
              <button class="action-btn delete-btn" @click.stop="del(row.id)">
                <DeleteOutlined /> 删除
              </button>
            </div>
          </template>
        </CustomTable>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { selectPageByAdmin, removeApp, setAppToFeatured } from '@/api/appController'
import CustomTable from './CustomTable.vue'
import {
  RocketOutlined,
  FolderOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  DeleteOutlined,
  FileImageOutlined,
  EyeOutlined,
  StarOutlined,
} from '@ant-design/icons-vue'

const openDropdown = ref<string | null>(null)

const toggleDropdown = (name: string) => {
  openDropdown.value = openDropdown.value === name ? null : name
}

const selectOption = (type: string, value: any) => {
  if (type === 'codeGenType') {
    searchParams.value.codeGenType = value
  } else if (type === 'priority') {
    searchParams.value.priority = value
  } else if (type === 'deployStatus') {
    deployStatus.value = value
  }
  openDropdown.value = null
}

const getLabel = (type: string, value: any) => {
  if (type === 'codeGenType') {
    return value === 'html' ? 'HTML' : value === 'multi_file' ? '多文件' : value === 'vue_project' ? 'Vue' : ''
  } else if (type === 'priority') {
    return value === 0 ? '普通' : value === 99 ? '精选' : ''
  } else if (type === 'deployStatus') {
    return value === 'deployed' ? '已部署' : value === 'not_deployed' ? '未部署' : ''
  }
  return ''
}

const closeDropdown = (e: MouseEvent) => {
  const target = e.target as HTMLElement
  if (!target.closest('.custom-select')) {
    openDropdown.value = null
  }
}

onMounted(() => {
  document.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
})

const PriorityEnum: Record<number, string> = {
  0: '普通',
  99: '精选',
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year} ${month}:${day} ${hours}:${minutes}:${seconds}`
}

const columns = [
  { key: 'id', title: 'ID', dataIndex: 'id', width: 60 },
  { key: 'cover', title: '封面', width: 80 },
  { key: 'appName', title: '应用名称', dataIndex: 'appName', width: 120 },
  { key: 'codeGenType', title: '代码类型', width: 100 },
  { key: 'deployKey', title: '部署状态', width: 100 },
  { key: 'priority', title: '作品等级', dataIndex: 'priority', width: 70 },
  { key: 'userVO', title: '创建者', width: 90 },
  { key: 'createTime', title: '创建时间', width: 160 },
  { key: 'action', title: '操作', width: 200 },
]

const data = ref<API.AppVO[]>([])
const total = ref(0)
const params = ref({ pageNum: 1, pageSize: 10 })
const searchParams = ref({
  appName: '',
  codeGenType: '',
  priority: undefined as number | undefined,
  userName: '',
  sortField: 'createTime',
  sortOrder: 'descend',
})
const deployStatus = ref('')

const load = async () => {
  const queryParams: any = {
    pageNum: params.value.pageNum,
    pageSize: params.value.pageSize,
    sortField: 'createTIme',
    sortOrder: 'descend',
  }

  if (searchParams.value.appName) {
    queryParams.appName = searchParams.value.appName
  }
  if (searchParams.value.codeGenType) {
    queryParams.codeGenType = searchParams.value.codeGenType
  }
  if (searchParams.value.priority !== undefined) {
    queryParams.priority = searchParams.value.priority
  }
  if (searchParams.value.userName) {
    queryParams.userName = searchParams.value.userName
  }

  const res = await selectPageByAdmin(queryParams)
  if (res.code === 200 && res.data) {
    let records = res.data.records || []
    if (deployStatus.value) {
      records = records.filter((app: API.AppVO) => {
        if (deployStatus.value === 'deployed') {
          return !!app.deployKey
        } else {
          return !app.deployKey
        }
      })
    }
    data.value = records
    total.value = res.data.totalRow || 0
  }
}

const handleSearch = () => {
  params.value.pageNum = 1
  load()
}

const handleReset = () => {
  searchParams.value = {
    appName: '',
    codeGenType: '',
    priority: undefined,
    userName: '',
    sortField: 'createTime',
    sortOrder: 'descend',
  }
  deployStatus.value = ''
  params.value.pageNum = 1
  load()
}

const onChange = (page: number, size: number) => {
  params.value.pageNum = page
  params.value.pageSize = size
  load()
}

const del = (id: number) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个应用吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      const res = await removeApp({ id })
      if (res.code === 200) {
        message.success('删除成功')
        load()
      } else {
        message.error('删除失败')
      }
    },
  })
}

const previewApp = (row: API.AppVO) => {
  const codeGenType = row.codeGenType || 'html'
  let previewUrl = '';
  if (codeGenType === 'vue_project') {
    previewUrl = `http://localhost:8123/api/static/${codeGenType}_${row.id}/dist/index.html`
  } else {
    previewUrl = `http://localhost:8123/api/static/${codeGenType}_${row.id}/`
  }
  window.open(previewUrl, '_blank')
}

const toggleFeatured = async (row: API.AppVO) => {
  try {
    const res = await setAppToFeatured({ id: row.id as number })
    if (res.code === 200) {
      message.success(row.priority === 99 ? '已取消精选' : '已设为精选')
      load()
    } else {
      message.error(res.message || '操作失败')
    }
  } catch (error) {
    message.error('操作失败，请重试')
  }
}

load()
</script>

<style scoped>
.app-manage-page {
  min-height: 100vh;
  padding: 90px 20px 40px;
  position: relative;
  background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 50%, #80deea 100%);
}

/* 背景装饰 */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 50%, #80deea 100%);
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.4;
  animation: float 15s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #84fab0, #8fd3f4);
  top: -100px;
  left: -100px;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #66ccff, #99eeff);
  bottom: -50px;
  right: -50px;
  animation-delay: -5s;
}

.orb-3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #ffd700, #ffec8b);
  top: 40%;
  left: 60%;
  animation-delay: -10s;
}

@keyframes float {

  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }

  50% {
    transform: translate(30px, -30px) scale(1.05);
  }
}

/* 内容区域 */
.content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
}

/* 标题 */
.header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  margin-left: -40px !important;
  font-size: 36px;
  font-weight: 800;
  color: #1a1a2e;
  margin: 0 0 8px;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  text-shadow: 2px 2px 4px rgba(102, 204, 255, 0.3);
}

.title-icon {
  font-size: 32px;
  color: #66ccff;
}

.subtitle {
  font-size: 16px;
  color: #4a5568;
  margin: 0;
}

.search-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.search-input-inline {
  height: 38px;
  padding: 0 14px;
  border: 1px solid rgba(102, 204, 255, 0.3);
  border-radius: 19px;
  font-size: 14px;
  color: #2d3748;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  transition: all 0.25s ease;
  outline: none;
  min-width: 140px;
}

.search-input-inline:focus {
  border-color: #66ccff;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 3px rgba(102, 204, 255, 0.15);
}

.search-input-inline::placeholder {
  color: #a0aec0;
}

.custom-select {
  position: relative;
  min-width: 110px;
}

.select-trigger {
  height: 38px;
  padding: 0 30px 0 14px;
  border: 1px solid rgba(102, 204, 255, 0.3);
  border-radius: 19px;
  font-size: 14px;
  color: #a0aec0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.25s ease;
}

.select-trigger.has-value {
  color: #4a5568;
}

.custom-select.is-open .select-trigger,
.select-trigger:hover {
  border-color: #66ccff;
  background: rgba(255, 255, 255, 0.95);
}

.custom-select.is-open .select-trigger {
  box-shadow: 0 0 0 3px rgba(102, 204, 255, 0.15);
}

.select-value {
  flex: 1;
}

.select-arrow {
  width: 0;
  height: 0;
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 5px solid #66ccff;
  transition: transform 0.25s ease;
  margin-left: 8px;
}

.custom-select.is-open .select-arrow {
  transform: rotate(180deg);
}

.select-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: white;
  border: 1px solid rgba(102, 204, 255, 0.2);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-8px);
  transition: all 0.2s ease;
  z-index: 100;
}

.custom-select.is-open .select-dropdown {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.select-option {
  padding: 10px 14px;
  font-size: 14px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
}

.select-option:hover {
  background: rgba(102, 204, 255, 0.1);
  color: #66ccff;
}

.select-option.is-selected {
  background: rgba(102, 204, 255, 0.15);
  color: #66ccff;
  font-weight: 500;
}

.select-option.is-selected::before {
  content: '';
  width: 6px;
  height: 6px;
  background: #66ccff;
  border-radius: 50%;
  margin-right: 8px;
}

.btn-search {
  height: 38px;
  padding: 0 24px;
  border: none;
  border-radius: 19px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #66ccff, #4fb3ff);
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(102, 204, 255, 0.3);
}

.btn-search:hover {
  background: linear-gradient(135deg, #4fb3ff, #33aaff);
  box-shadow: 0 6px 16px rgba(102, 204, 255, 0.4);
  transform: translateY(-1px);
}

.btn-search:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(102, 204, 255, 0.3);
}

/* 标签样式 */
.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.tag-multi {
  background: linear-gradient(135deg, #e0f7ff, #b3e5fc);
  color: #0288d1;
}

.tag-html {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #f57c00;
}
.tag-vue {
  background: linear-gradient(135deg, #b4f1d2, #92efc2);
  color: #0a8d64;
}

.tag-deployed {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  color: #388e3c;
}

.tag-not-deployed {
  background: linear-gradient(135deg, #fff8e1, #ffecb3);
  color: #f9a825;
}

.user-name {
  font-weight: 500;
  color: #1a1a2e;
}

.time-text {
  white-space: nowrap;
  font-size: 12px;
  color: #4a5568;
}

.cover-img {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: cover;
  border: 2px solid rgba(102, 204, 255, 0.3);
}

.cover-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, #84fab0, #8fd3f4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}

.priority-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

.priority-normal {
  background: rgba(102, 204, 255, 0.15);
  color: #66ccff;
}

.priority-top {
  background: linear-gradient(135deg, #ffd700, #ffec8b);
  color: #b8860b;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 4px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 4px 8px;
  border: none;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.preview-btn {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #1565c0;
}

.preview-btn:hover {
  background: linear-gradient(135deg, #bbdefb, #90caf9);
  transform: scale(1.05);
}

.featured-btn {
  background: linear-gradient(135deg, #fff8e1, #ffecb3);
  color: #f9a825;
}

.featured-btn:hover {
  background: linear-gradient(135deg, #ffecb3, #ffe082);
  transform: scale(1.05);
}

.featured-btn.is-featured {
  background: linear-gradient(135deg, #ffd700, #ffec8b);
  color: #b8860b;
}

.featured-btn.is-featured:hover {
  background: linear-gradient(135deg, #ffb300, #ffc107);
}

.delete-btn {
  background: linear-gradient(135deg, #ffebee, #ffcdd2);
  color: #d32f2f;
}

.delete-btn:hover {
  background: linear-gradient(135deg, #ffcdd2, #ef9a9a);
  transform: scale(1.05);
}

/* 响应式 */
@media (max-width: 768px) {
  .app-manage-page {
    padding: 80px 12px 20px;
  }

  .title {
    font-size: 24px;
  }

  .title-icon {
    font-size: 24px;
  }
}
</style>
