<template>
  <a-result status="403" title="403" sub-title="你没有权限访问此页面">
    <template #extra>
      <a-button type="primary" @click="toHomeView">首页</a-button>
    </template>
  </a-result>
</template>
<script>
import { useRouter } from 'vue-router'

const router = useRouter()

const toHomeView = () => {
  router.push('/')
}
</script>
