<template>
  <div id="UserManagePage">
    <a-table :columns="columns" :data-source="data">
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
            <a-button type="primary" ghost>编辑</a-button>
          </span>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { list } from '@/api/userController'
import { SmileOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { ref } from 'vue'
import { onMounted } from 'vue'
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
onMounted(async () => {
  const res = await list()
  if (res.code === 200 && res.data) {
    res.data.forEach((item) => {
      item.userRole = item.userRole?.split(',') as any // 将字符串转换为数组
    })
    data.value = res.data as any[]
    console.log(data.value)
  } else {
    message.error(res.message || '获取用户列表失败')
  }
})
</script>
