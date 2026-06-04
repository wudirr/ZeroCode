<template>
  <div class="element-info-bar">
    <div class="info-header">
      <div class="info-title">
        <svg class="info-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
          />
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
          />
        </svg>
        <span>已选中 {{ elements.length }} 个元素</span>
      </div>
      <button @click="$emit('clear')" class="clear-all-btn">清空</button>
    </div>

    <div class="info-list">
      <div v-for="(el, idx) in elements" :key="idx" class="element-card">
        <div class="element-card-main">
          <span class="tag-badge">&lt;{{ el.tagName }}&gt;</span>
          <span v-if="el.id" class="id-badge">#{{ el.id }}</span>
          <span v-if="el.textContent" class="text-preview">{{ truncate(el.textContent, 40) }}</span>
          <span v-if="!el.textContent && el.className" class="class-preview">.{{ truncate(el.className, 30) }}</span>
        </div>
        <button @click="$emit('remove', idx)" class="remove-btn" title="移除此元素">&times;</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ElementInfo } from '@/composables/useVisualEditor'

defineProps<{
  elements: ElementInfo[]
}>()

defineEmits<{
  remove: [index: number]
  clear: []
}>()

function truncate(str: string, max: number): string {
  if (str.length <= max) return str
  return str.substring(0, max) + '...'
}
</script>

<style scoped>
.element-info-bar {
  background: linear-gradient(135deg, rgba(102, 204, 255, 0.08) 0%, rgba(99, 102, 241, 0.06) 100%);
  border: 1px solid rgba(102, 204, 255, 0.25);
  border-radius: 12px;
  padding: 10px 16px;
  margin: 0 16px;
  animation: slideDown 0.25s ease-out;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.info-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #3b82f6;
}

.info-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.clear-all-btn {
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 12px;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 6px;
  transition: all 0.2s;
  font-family: 'Fira Sans', sans-serif;
}

.clear-all-btn:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.08);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.element-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  border: 1px solid rgba(59, 130, 246, 0.12);
  transition: all 0.2s;
}

.element-card:hover {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(59, 130, 246, 0.25);
}

.element-card-main {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.tag-badge {
  display: inline-flex;
  align-items: center;
  padding: 1px 6px;
  background: linear-gradient(135deg, #dbeafe, #ede9fe);
  color: #4f46e5;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  font-family: 'Fira Code', monospace;
  white-space: nowrap;
  flex-shrink: 0;
}

.id-badge {
  display: inline-flex;
  align-items: center;
  padding: 1px 6px;
  background: #fef3c7;
  color: #92400e;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  font-family: 'Fira Code', monospace;
  white-space: nowrap;
  flex-shrink: 0;
}

.text-preview,
.class-preview {
  font-size: 12px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-btn {
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 16px;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
  transition: color 0.2s;
  flex-shrink: 0;
}

.remove-btn:hover {
  color: #ef4444;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
