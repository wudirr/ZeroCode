<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        <a
          href="https://www.codefather.cn"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
          版权归zelin所有
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: transparent;
  text-align: center;
  padding: 20px;
}

.copyright {
  margin: 0;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

.author-link {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: color 0.2s;
}

.author-link:hover {
  color: #fff;
}
</style>
