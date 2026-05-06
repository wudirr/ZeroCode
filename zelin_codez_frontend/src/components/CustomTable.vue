<template>
  <div class="custom-table">
    <!-- 装饰 -->
    <div class="decorations">
      <StarFilled class="star star-1" />
      <StarFilled class="star star-2" />
      <CloudOutlined class="cloud cloud-1" />
      <CloudOutlined class="cloud cloud-2" />
    </div>

    <div class="table-container">
      <table class="table">
        <thead class="thead">
          <tr>
            <th v-for="col in columns" :key="col.key" :style="{ width: col.width + 'px' }">
              <span class="th-content">{{ col.title }}</span>
            </th>
          </tr>
        </thead>
        <tbody class="tbody">
          <tr
            v-for="(row, index) in data"
            :key="index"
            class="data-row"
            :class="{ 'row-odd': index % 2 === 1 }"
          >
            <td v-for="col in columns" :key="col.key" class="data-cell">
              <slot :name="col.key" :row="row" :value="row[col.dataIndex]">
                {{ row[col.dataIndex] }}
              </slot>
            </td>
          </tr>
          <tr v-if="data.length === 0">
            <td :colspan="columns.length" class="empty-cell">
              <InboxOutlined class="empty-icon" />
              <span>暂无数据</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination">
      <div class="page-size-selector">
        <span class="size-label">每页</span>
        <select class="size-select" :value="pageSize" @change="changeSize($event)">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        <span class="size-label">条</span>
      </div>
      <button class="page-btn" :disabled="pageNum <= 1" @click="changePage(pageNum - 1)">
        <LeftOutlined /> 上一页
      </button>
      <div class="page-info">
        <span class="page-number">{{ pageNum }}</span>
        <span class="page-divider">/</span>
        <span class="page-total">{{ totalPage }}</span>
      </div>
      <button class="page-btn" :disabled="pageNum >= totalPage" @click="changePage(pageNum + 1)">
        下一页 <RightOutlined />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  StarFilled,
  CloudOutlined,
  InboxOutlined,
  LeftOutlined,
  RightOutlined,
} from '@ant-design/icons-vue'

const props = defineProps<{
  columns: { key: string; title: string; dataIndex?: string; width?: number }[]
  data: any[]
  total: number
  pageNum: number
  pageSize: number
}>()

const emit = defineEmits<{
  (e: 'change', page: number, size: number): void
}>()

const totalPage = computed(() => Math.ceil(props.total / props.pageSize) || 1)

const changePage = (page: number) => {
  emit('change', page, props.pageSize)
}

const changeSize = (event: Event) => {
  const size = parseInt((event.target as HTMLSelectElement).value)
  emit('change', 1, size)
}
</script>

<style scoped>
.custom-table {
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 24px;
  box-shadow:
    0 8px 32px rgba(102, 204, 255, 0.15),
    0 2px 8px rgba(102, 204, 255, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 3px solid rgba(102, 204, 255, 0.3);
  overflow: hidden;
}

/* 装饰元素 */
.decorations {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 40px;
  pointer-events: none;
  overflow: hidden;
}

.star {
  position: absolute;
  color: #ffd700;
  font-size: 14px;
  animation: twinkle 2s ease-in-out infinite;
}

.star-1 {
  top: 8px;
  left: 15%;
  animation-delay: 0s;
}
.star-2 {
  top: 12px;
  right: 20%;
  animation-delay: 0.5s;
  font-size: 10px;
}

.cloud {
  position: absolute;
  color: rgba(102, 204, 255, 0.4);
  font-size: 16px;
  animation: float 4s ease-in-out infinite;
}

.cloud-1 {
  top: 6px;
  right: 10%;
  animation-delay: 0s;
}
.cloud-2 {
  top: 10px;
  left: 5%;
  animation-delay: 1s;
  font-size: 12px;
}

@keyframes twinkle {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateX(0);
  }
  50% {
    transform: translateX(10px);
  }
}

/* 表格容器 */
.table-container {
  overflow-x: auto;
  margin: 0 -8px;
  padding: 0 8px;
}

.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0 8px;
}

/* 表头 */
.thead th {
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  padding: 14px 16px;
  text-align: center;
  font-weight: 600;
  color: #1a1a2e;
  font-size: 14px;
  position: relative;
  border: none;
  border-bottom: 2px solid rgba(102, 204, 255, 0.3);
}

.thead th:first-child {
  border-radius: 16px 0 0 16px;
}

.thead th:last-child {
  border-radius: 0 16px 16px 0;
}

.thead th::after {
  display: none;
}

.th-content {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* 表格行 */
.data-row {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.data-row td {
  background: rgba(255, 255, 255, 0.9);
  padding: 14px 16px;
  text-align: center;
  color: #4a5568;
  font-size: 13px;
  border: none;
  position: relative;
}

.data-row td:first-child {
  border-radius: 16px 0 0 16px;
  border-left: 3px solid rgba(102, 204, 255, 0.5);
}

.data-row td:last-child {
  border-radius: 0 16px 16px 0;
  border-right: 3px solid rgba(102, 204, 255, 0.5);
}

/* 奇数行 */
.row-odd td {
  background: rgba(132, 250, 176, 0.15);
}

/* 悬停效果 */
.data-row:hover td {
  background: linear-gradient(135deg, rgba(102, 204, 255, 0.25) 0%, rgba(153, 238, 255, 0.25) 100%);
  transform: translateY(-3px);
  box-shadow:
    0 8px 20px rgba(102, 204, 255, 0.25),
    0 2px 8px rgba(102, 204, 255, 0.15);
}

.data-row:hover td:first-child {
  border-left-color: #66ccff;
}

.data-row:hover td:last-child {
  border-right-color: #66ccff;
}

/* 点击效果 */
.data-row:active td {
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(102, 204, 255, 0.15);
}

/* 空状态 */
.empty-cell {
  padding: 40px !important;
  text-align: center;
  color: #a0aec0;
  background: transparent !important;
  border-radius: 16px !important;
  border: none !important;
}

.empty-icon {
  display: block;
  font-size: 32px;
  color: rgba(102, 204, 255, 0.5);
  margin-bottom: 8px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 2px dashed rgba(102, 204, 255, 0.2);
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 16px;
}

.size-label {
  color: #4a5568;
  font-size: 13px;
  font-weight: 500;
}

.size-select {
  padding: 6px 10px;
  border: 1px solid rgba(102, 204, 255, 0.4);
  border-radius: 8px;
  background: #fff;
  color: #1a1a2e;
  font-size: 13px;
  cursor: pointer;
  outline: none;
}

.size-select:hover {
  border-color: #66ccff;
}

.size-select:focus {
  border-color: #66ccff;
  box-shadow: 0 0 0 2px rgba(102, 204, 255, 0.2);
}

.size-select option {
  padding: 8px 12px;
  background: #fff;
  color: #1a1a2e;
  border-radius: 4px;
}

.size-select option:hover {
  background: rgba(102, 204, 255, 0.1);
  color: #66ccff;
}

.page-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: 2px solid #66ccff;
  background: linear-gradient(135deg, #fff 0%, rgba(102, 204, 255, 0.1) 100%);
  color: #1a1a2e;
  border-radius: 20px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(102, 204, 255, 0.15);
}

.page-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #66ccff 0%, #99eeff 100%);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 204, 255, 0.3);
}

.page-btn:active:not(:disabled) {
  transform: scale(0.95);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  box-shadow: none;
}

.page-info {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background: rgba(102, 204, 255, 0.1);
  border-radius: 16px;
}

.page-number {
  font-weight: 700;
  color: #66ccff;
  font-size: 16px;
}

.page-divider {
  color: #a0aec0;
}

.page-total {
  color: #4a5568;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .custom-table {
    padding: 16px;
    border-radius: 16px;
  }

  .table-container {
    margin: 0 -4px;
    padding: 0 4px;
  }

  .thead th,
  .data-row td {
    padding: 10px 8px;
    font-size: 12px;
  }

  .pagination {
    gap: 8px;
  }

  .page-btn {
    padding: 8px 12px;
    font-size: 12px;
  }
}
</style>
