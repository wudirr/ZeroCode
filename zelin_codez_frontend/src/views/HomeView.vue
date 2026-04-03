<template>
  <div class="HomeViewHome-View">
    这个是主页
    <a-divider></a-divider>
    <a-space>
      <a-button type="primary" @click="apiTest">朴素按钮</a-button>
      <a-button danger>危险按钮</a-button>
      <a-button ghost>幽灵按钮</a-button>
    </a-space>
    <a-divider></a-divider>
    <a-tooltip placement="topLeft">
      <template #title>
        <span>prompt text</span>
      </template>
      <a-button>TL</a-button>
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
import { hello } from '@/api/helloController'
import { ref, reactive, onMounted } from 'vue'
const apiTest = () => {
  hello().then((res) => {
    console.log(res);
  })
}
// 生命周期
onMounted(() => {
  console.log('组件已挂载')
})
</script>

<style scoped>
#HomeViewHome-View {

}
</style>
