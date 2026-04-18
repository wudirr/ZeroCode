<template>
  <div id="UserManagePage">
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="handleTableChange"
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
          <a>
            {{ record.userName }}
          </a>
        </template>
        <template v-else-if="column.key === 'userAvatar'">
          <span>
            <a-image :width="42" :src="record.userAvatar" />
          </span>
        </template>
        <template v-else-if="column.key === 'userRole'">
          <span>
            <a-tag
              v-for="tag in record.userRole"
              :key="tag"
              :color="tag === 'admin' ? 'volcano' : tag.length > 5 ? 'geekblue' : 'green'"
            >
              <div v-if="tag === 'admin'">管理员</div>
              <div v-else-if="tag === 'user'">用户</div>
            </a-tag>
          </span>
        </template>
        <template v-else-if="column.key === 'action'">
          <span>
            <a-button danger @click="confirm(record.id)">删除</a-button>
          </span>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { page, remove } from '@/api/userController'
import { message, Modal } from 'ant-design-vue'
import { computed, createVNode, ref } from 'vue'
import { onMounted } from 'vue'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'
const columns = [
  {
    name: 'ID',
    dataIndex: 'id',
    key: 'id',
    align: 'center',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    key: 'userName',
    align: 'center',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    key: 'userAvatar',
    align: 'center',
  },
  {
    title: '用户简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
    align: 'center',
  },
  {
    title: '角色',
    key: 'userRole',
    dataIndex: 'userRole',
    align: 'center',
  },
  {
    title: '操作',
    key: 'action',
    align: 'center',
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
#UserManagePage {
  min-width: 768px;
}
</style>
