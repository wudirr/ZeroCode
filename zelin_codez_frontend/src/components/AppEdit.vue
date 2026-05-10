<template>
  <div id="AppEdit">
    <div class="edit-container">
      <div class="edit-card">
        <div class="card-header">
          <h2>编辑应用</h2>
        </div>

        <div class="form-content">
          <div class="form-item cover-item">
            <label>封面图片</label>
            <div class="cover-preview">
              <img v-if="appDetail.cover" :src="appDetail.cover" alt="封面" class="cover-image" />
              <div v-else class="cover-placeholder">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M2.25 15.75a5.25 5.25 0 01.206-.532l2.52-4.5a5.25 5.25 0 019.196 0l2.52 4.5a5.25 5.25 0 01.206.532l-9.414 14.25a5.25 5.25 0 01-4.95 0l-9.414-14.25z"
                  />
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M3.75 6.75h.75v.75h-.75v-.75zM6.75 15h.75v.75h-.75v-.75zM3 9.75h18v8.25H3V9.75z"
                  />
                </svg>
              </div>
            </div>
          </div>

          <div class="form-item">
            <label>应用名称</label>
            <input
              v-model="appEditParam.appName"
              type="text"
              class="form-input"
              placeholder="请输入应用名称"
            />
          </div>

          <div class="form-item">
            <label>初始提示词</label>
            <textarea
              v-model="appDetail.initPrompt"
              class="form-textarea"
              rows="4"
              placeholder="初始提示词"
              readonly
            ></textarea>
            <span class="form-hint">此字段不可编辑</span>
          </div>

          <div class="form-item">
            <label>代码类型</label>
            <input
              v-model="appDetail.codeGenType"
              type="text"
              class="form-input readonly"
              readonly
              placeholder="代码类型"
            />
            <span class="form-hint">此字段不可编辑</span>
          </div>

          <div class="form-item">
            <label>部署标识</label>
            <input
              v-model="appDetail.deployKey"
              type="text"
              class="form-input readonly"
              readonly
              placeholder="部署标识"
            />
            <span class="form-hint">此字段不可编辑</span>
          </div>
        </div>

        <div class="card-footer">
          <button class="btn-secondary" @click="goBack">返回</button>
          <button class="btn-primary" @click="handleSave">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getAppInfo, editApp } from '@/api/appController'
import { message } from 'ant-design-vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const appId = ref(route.params.appId as string)
const appDetail = ref<API.AppVO>({})
const appEditParam = ref({
  id: 0,
  appName: '',
})

onMounted(async () => {
  const res = await getAppInfo({ id: appId.value as any })
  if (res.code === 200 && res.data) {
    appDetail.value = res.data || {}
    appEditParam.value = {
      id: appDetail.value.id as any,
      appName: appDetail.value.appName || '',
    }
  } else {
    message.error(`获取应用详情失败: ${res.message}`)
  }
})

const handleSave = async () => {
  if (!appEditParam.value.appName?.trim()) {
    message.warning('请输入应用名称')
    return
  }

  try {
    const res = await editApp({
      id: appEditParam.value.id,
      appName: appEditParam.value.appName,
    } as API.AppEditRequest)

    if (res.code === 200) {
      message.success('保存成功')
      setTimeout(() => {
        goBack()
      }, 1000)
    } else {
      message.error(res.message || '保存失败')
    }
  } catch (error) {
    message.error('保存失败，请重试')
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fira+Sans:wght@300;400;500;600;700&display=swap');

#AppEdit {
  font-family: 'Fira Sans', sans-serif;
  min-height: 100vh;
  background-image:
    linear-gradient(60deg, #64b3f4 0%, #c2e59c 100%),
    radial-gradient(73% 147%, #eadfdf 59%, #ece2df 100%);
  background-blend-mode: screen;
  background-color: #f0f9ff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.edit-container {
  width: 100%;
  max-width: 500px;
}

.edit-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  padding: 24px;
  background: rgba(255, 255, 255, 0.5);
  border-bottom: 1px solid rgba(226, 232, 240, 0.5);
  text-align: center;
}

.card-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
}

.form-content {
  padding: 24px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  font-family: 'Fira Sans', sans-serif;
  color: #1e293b;
  background: #fff;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.readonly,
.form-textarea:read-only {
  background: #f8fafc;
  color: #64748b;
  cursor: not-allowed;
}

.form-textarea {
  resize: none;
}

.form-hint {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.cover-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cover-preview {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  overflow: hidden;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  color: #94a3b8;
}

.cover-placeholder svg {
  width: 40px;
  height: 40px;
}

.card-footer {
  padding: 16px 24px;
  display: flex;
  gap: 12px;
  border-top: 1px solid rgba(226, 232, 240, 0.5);
  background: rgba(255, 255, 255, 0.3);
}

.btn-primary,
.btn-secondary {
  flex: 1;
  padding: 12px 20px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  font-family: 'Fira Sans', sans-serif;
  cursor: pointer;
  transition: all 0.2s;
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
</style>
