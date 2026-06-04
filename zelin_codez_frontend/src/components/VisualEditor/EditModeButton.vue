<template>
  <button
    @click="$emit('toggle')"
    :disabled="disabled"
    :class="['edit-mode-btn', { active: isEditMode }]"
    :title="isEditMode ? '退出编辑模式' : '进入编辑模式'"
  >
    <svg
      class="btn-icon"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
    >
      <path
        v-if="!isEditMode"
        stroke-linecap="round"
        stroke-linejoin="round"
        d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
      />
      <path
        v-else
        stroke-linecap="round"
        stroke-linejoin="round"
        d="M6 18L18 6M6 6l12 12"
      />
    </svg>
    <span class="btn-text">{{ isEditMode ? '退出' : '编辑' }}</span>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  isEditMode: boolean
  disabled?: boolean
}>()

defineEmits<{
  toggle: []
}>()
</script>

<style scoped>
.edit-mode-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px 14px;
  background: #e2e8f0;
  color: #475569;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-family: 'Fira Sans', sans-serif;
  font-size: 13px;
  transition: all 0.2s;
  white-space: nowrap;
}

.edit-mode-btn:hover:not(:disabled) {
  background: #cbd5e1;
  transform: translateY(-1px);
}

.edit-mode-btn.active {
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.edit-mode-btn.active:hover:not(:disabled) {
  background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.edit-mode-btn:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.btn-text {
  font-weight: 500;
}
</style>
