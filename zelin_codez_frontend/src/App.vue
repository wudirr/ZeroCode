<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { themeConfig } from './theme/themeConfig';
import { ConfigProvider } from 'ant-design-vue';
</script>

<template>
  <ConfigProvider :theme="themeConfig"></ConfigProvider>
  <BasicLayout />
</template>

<style>
.ant-btn-default.ant-btn-background-ghost{
  border-color: #00c2ff;
  color: #00c2ff;
}
</style>
