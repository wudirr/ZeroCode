<template>
  <div class="user-manage-page">
    <!-- 动态渐变背景 -->
    <div class="bg-gradient">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
    </div>

    <div class="content-wrapper">
      <div class="page-header">
        <h1>用户管理</h1>
        <p>管理系统用户信息</p>
      </div>

      <div class="table-container">
        <a-table
          :columns="columns"
          :data-source="data"
          :pagination="pagination"
          @change="handleTableChange"
          :scroll="{ x: 700 }"
        >
          <template #headerCell="{ column }">
            <template v-if="column.key === 'id'">
              <span>
                <smile-outlined />
                ID
              </span>
            </template>
          </template>

          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'userName'">
              <span class="user-name">{{ record.userName }}</span>
            </template>
            <template v-else-if="column.key === 'userAvatar'">
              <a-avatar :src="record.userAvatar" :size="42" />
            </template>
            <template v-else-if="column.key === 'userRole'">
              <a-tag :color="record.userRole?.includes('admin') ? '#66ccff' : '#84fab0'">
                {{ record.userRole?.includes('admin') ? '管理员' : '用户' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button danger size="small" @click="confirm(record.id)"> 删除 </a-button>
            </template>
          </template>
        </a-table>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { page, remove } from '@/api/userController'
import { message, Modal } from 'ant-design-vue'
import { computed, createVNode, ref } from 'vue'
import { onMounted } from 'vue'
import { ExclamationCircleOutlined, SmileOutlined } from '@ant-design/icons-vue'
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 100,
    align: 'center',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    key: 'userName',
    width: 120,
    align: 'center',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    key: 'userAvatar',
    width: 100,
    align: 'center',
  },
  {
    title: '用户简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
    width: 150,
    ellipsis: true,
    align: 'center',
  },
  {
    title: '角色',
    key: 'userRole',
    dataIndex: 'userRole',
    width: 100,
    align: 'center',
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    align: 'center',
    fixed: 'right',
  },
]

const data = ref([{}])

const deleteId = ref<string>()

const total = ref(0)
const searchParams = ref<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

const pagination = computed(() => {
  return {
    current: searchParams.value.pageNum ?? 1,
    pageSize: searchParams.value.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共${total}条`,
  }
})

const handleTableChange = (pagination: any) => {
  searchParams.value.pageNum = pagination.current
  searchParams.value.pageSize = pagination.pageSize
  fetchData()
}
async function fetchData() {
  const res = await page({
    ...(searchParams.value as any),
  })
  if (res.code === 200 && res.data?.records) {
    res.data.records.forEach((item) => {
      item.userRole = item.userRole?.split(',') as any // 将字符串转换为数组
    })
    data.value = res.data.records as any[]
    total.value = res.data.totalRow ?? 0
    console.log(res.data)
  } else {
    message.error(res.message || '获取用户列表失败')
  }
}

const handlerDelete = async () => {
  const id = deleteId.value
  const res = await remove({ id })
  if (res.code === 200 && res.data) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error(res.message || '删除失败')
  }
  console.log('aaa')
}

const confirm = (id: string) => {
  deleteId.value = id
  Modal.confirm({
    title: '警告',
    icon: createVNode(ExclamationCircleOutlined),
    content: '您确定要删除该用户吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: () => handlerDelete(),
  })
}
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.user-manage-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  padding-top: 80px;
}

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
  width: 500px;
  height: 500px;
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
  top: -150px;
  left: -100px;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(120deg, #66ccff 0%, #99eeff 100%);
  bottom: -100px;
  right: -50px;
  animation-delay: -10s;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(20px, -20px) scale(1.02);
  }
  66% {
    transform: translate(-15px, 15px) scale(0.98);
  }
}

.content-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px 40px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 16px;
  color: #4a5568;
  margin: 0;
}

.table-container {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(102, 204, 255, 0.15);
  border: 1px solid rgba(102, 204, 255, 0.2);
}

.user-name {
  font-weight: 600;
  color: #1a1a2e;
}

/* 表格样式覆盖 */
.user-manage-page :deep(.ant-table) {
  background: transparent;
}

.user-manage-page :deep(.ant-table-thead > tr > th) {
  background: rgba(255, 255, 255, 0.9) !important;
  color: #1a1a2e;
  font-weight: 600;
  border-bottom: 1px solid rgba(102, 204, 255, 0.3);
}

.user-manage-page :deep(.ant-table-tbody > tr > td) {
  background: rgba(255, 255, 255, 0.6) !important;
  border-bottom: 1px solid rgba(102, 204, 255, 0.15);
  color: #4a5568;
}

.user-manage-page :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(102, 204, 255, 0.15) !important;
}

.user-manage-page :deep(.ant-table-column-sorter) {
  color: #66ccff;
}

.user-manage-page :deep(.ant-pagination) {
  margin-top: 16px;
}

.user-manage-page :deep(.ant-pagination-item-active) {
  border-color: #66ccff;
}

.user-manage-page :deep(.ant-pagination-item-active a) {
  color: #66ccff;
}

.user-manage-page :deep(.ant-btn-dangerous) {
  background: rgba(255, 255, 255, 0.9);
  border-color: #ff4d4f;
  color: #ff4d4f;
}

.user-manage-page :deep(.ant-btn-dangerous:hover) {
  background: #ff4d4f;
  border-color: #ff4d4f;
  color: #fff;
}

.user-manage-page :deep(.ant-tag) {
  border-radius: 8px;
  font-weight: 500;
}

.user-manage-page :deep(.ant-avatar) {
  border: 2px solid rgba(102, 204, 255, 0.3);
}
</style>
