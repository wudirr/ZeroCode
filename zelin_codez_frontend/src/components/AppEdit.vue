<template>
  <div id="AppEditApp-Edit">
      <div class="app-edit-name">
        应用名称:
        <a-input v-model:value="appEditParam!.appName"></a-input>
      </div>

      <div class="app-edit-initprompt">
        初始提示词:
        <a-textarea v-model:value="appDetail.initPrompt" :rows="4"></a-textarea>
      </div>

      <div class="app-edit-codeGenType">
        生成类型:
        <a-input v-model:value="appDetail.codeGenType"></a-input>
      </div>

      <div class="app-edit-depoloyKey">
        部署标识:
        <a-input v-model:value="appDetail.deployKey"></a-input>
      </div>
  </div>
</template>

<script setup lang="ts">
import { getAppInfo } from '@/api/appController'
import { message } from 'ant-design-vue'
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const appId = ref(route.params.appId as string)
const appDetail = ref<API.AppVO>({})
const appEditParam = ref<API.AppEditRequest>({
  id: 0,
  appName: '',
  cover: '',
})
// 生命周期
onMounted(async () => {
  //获取应用详情
  console.log('appId', appId.value)
  const res = await getAppInfo({ id: appId.value as any })
  if (res.code === 200 && res.data) {
    appDetail.value = res.data || {}
    appEditParam.value = {
      ...appDetail.value,
      id: appDetail.value.id as any,
    }
  } else {
    message.error(`获取应用详情失败:${res.message}`)
  }
})
</script>

<style scoped></style>
