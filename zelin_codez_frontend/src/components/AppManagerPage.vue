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

      <!-- 表格区域 -->
      <div class="table-section">
        <CustomTable
          :columns="columns"
          :data="data"
          :total="total"
          :page-num="params.pageNum"
          :page-size="params.pageSize"
          @change="onChange"
        >
          <template #cover="{ row }">
            <img v-if="row.cover" :src="row.cover" class="cover-img" />
            <div v-else class="cover-placeholder">
              <FileImageOutlined />
            </div>
          </template>
          <template #codeGenType="{ row }">
            <span class="tag" :class="row.codeGenType === 'multi_file' ? 'tag-multi' : 'tag-html'">
              <FolderOutlined v-if="row.codeGenType === 'multi_file'" />
              <FileTextOutlined v-else />
              {{ row.codeGenType === 'multi_file' ? '多文件' : 'HTML' }}
            </span>
          </template>
          <template #priority="{ row }">
            <span
              class="priority-tag"
              :class="row.priority === 99 ? 'priority-top' : 'priority-normal'"
            >
              {{ PriorityEnum[row.priority] || '普通' }}
            </span>
          </template>
          <template #deployKey="{ row }">
            <span class="tag" :class="row.deployKey ? 'tag-deployed' : 'tag-not-deployed'">
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
            <button class="delete-btn" @click.stop="del(row.id)"><DeleteOutlined /> 删除</button>
          </template>
        </CustomTable>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { selectPageByAdmin, removeApp } from '@/api/appController'
import CustomTable from './CustomTable.vue'
import {
  RocketOutlined,
  FolderOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  DeleteOutlined,
  FileImageOutlined,
} from '@ant-design/icons-vue'

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
  { key: 'priority', title: '优先级', dataIndex: 'priority', width: 70 },
  { key: 'userVO', title: '创建者', width: 90 },
  { key: 'createTime', title: '创建时间', width: 160 },
  { key: 'action', title: '操作', width: 80 },
]

const data = ref<API.AppVO[]>([])
const total = ref(0)
const params = ref({ pageNum: 1, pageSize: 10 })

const load = async () => {
  const res = await selectPageByAdmin(params.value as any)
  if (res.code === 200 && res.data) {
    data.value = res.data.records || []
    total.value = res.data.totalRow || 0
  }
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

/* 标签样式 */
.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.tag-multi {
  background: linear-gradient(135deg, #e0f7ff, #b3e5fc);
  color: #0288d1;
}

.tag-html {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #f57c00;
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
}

.priority-normal {
  background: rgba(102, 204, 255, 0.15);
  color: #66ccff;
}

.priority-top {
  background: linear-gradient(135deg, #ffd700, #ffec8b);
  color: #b8860b;
}

/* 删除按钮 */
.delete-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  background: linear-gradient(135deg, #ffebee, #ffcdd2);
  color: #d32f2f;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
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
