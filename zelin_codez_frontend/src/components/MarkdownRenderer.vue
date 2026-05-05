<template>
  <div id="MarkdownRenderer">
    <div class="markdown-body" v-html="rendererHtml"></div>
  </div>
</template>

<script setup lang="ts">
import MarkdownIt from 'markdown-it'
import 'highlight.js/styles/a11y-light.css'
import hljs from 'highlight.js'
import { computed, watch, nextTick, ref } from 'vue'

let highlightTimeout: any = null
const isTyping = ref(false)

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true,
  highlight: function (str: string, lang: any) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return (
          '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang }).value +
          '</code></pre>'
        )
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  },
})
const props = defineProps({
  content: {
    type: String,
    default: '',
  },
})
const rendererHtml = computed(() => {
  if (!props.content) return ''
  return md.render(props.content)
})

watch(
  () => props.content,
  (newVal, oldVal) => {
    if (!newVal) return

    // 清除之前的定时器
    if (highlightTimeout) clearTimeout(highlightTimeout)

    // 内容稳定后高亮，避免连续输入时抖动
    highlightTimeout = setTimeout(() => {
      nextTick().then(() => {
        document.querySelectorAll('.markdown-body pre code').forEach((el) => {
          hljs.highlightElement(el)
        })
      })
    }, 150)
  },
)
</script>

<style scoped>
/* 基础样式 */
.markdown-body {
  line-height: 1.6;
  font-size: 16px;
  padding: 12px;
  max-width: 100%;
  color: #1e293b;
  background: transparent;
  word-wrap: break-word;
  overflow-wrap: break-word;
  overflow-x: visible;
}

.markdown-body pre {
  border-radius: 12px;
  padding: 16px;
  overflow-x: auto;
  overflow-y: hidden;
  margin: 16px 0;
  background: #1e293b;
  width: 100%;
  min-width: 0;
  max-width: 100%;
  word-wrap: normal;
  white-space: pre;
  display: block;
  box-sizing: border-box;
  position: relative;
  border: 1px solid #334155;
}

.markdown-body pre code {
  display: inline-block;
  width: auto;
  min-width: 100%;
  max-width: 100%;
  overflow-x: auto;
  white-space: pre;
  word-wrap: normal;
  font-family: 'Fira Code', 'Cascadia Code', 'Consolas', monospace;
  font-size: 14px;
}

.markdown-body code {
  display: inline;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

:deep(.markdown-body pre),
:deep(.markdown-body code) {
  font-family: 'Fira Code', 'Cascadia Code', 'Consolas', monospace !important;
  font-size: 14px;
}

/* 行内代码样式 */
.markdown-body :not(pre) > code {
  background: #334155;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
  color: #f8fafc;
}

.markdown-body h1 {
  font-size: 24px;
  margin: 24px 0 16px;
  border-bottom: 1px solid #334155;
  color: #f8fafc;
  word-wrap: break-word;
}

.markdown-body h2 {
  font-size: 20px;
  margin: 20px 0 12px;
  border-bottom: 1px solid #334155;
  color: #f8fafc;
  word-wrap: break-word;
}

.markdown-body h3 {
  font-size: 18px;
  margin: 18px 0 10px;
  color: #f8fafc;
  word-wrap: break-word;
}

.markdown-body p {
  margin: 16px 0;
  color: #f8fafc;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.markdown-body h2 {
  font-size: 20px;
  margin: 20px 0 12px;
  border-bottom: 1px solid #334155;
  color: #f8fafc;
}

.markdown-body h3 {
  font-size: 18px;
  margin: 18px 0 10px;
  color: #f8fafc;
}

.markdown-body p {
  margin: 16px 0;
  color: #f8fafc;
}

.markdown-body ul,
.markdown-body ol {
  margin: 16px 0;
  padding-left: 32px;
  color: #f8fafc;
  word-wrap: break-word;
}

.markdown-body li {
  margin: 8px 0;
  color: #f8fafc;
  word-wrap: break-word;
}

.markdown-body li {
  margin: 8px 0;
  color: #f8fafc;
}

.markdown-body blockquote {
  border-left: 4px solid #22c55e;
  padding-left: 16px;
  margin: 16px 0;
  color: #cbd5e1;
  background: #1e293b;
  padding: 12px 16px;
  border-radius: 4px;
}

.markdown-body table {
  border-collapse: collapse;
  margin: 16px 0;
  width: 100%;
  background: #1e293b;
}

.markdown-body th,
.markdown-body td {
  border: 1px solid #334155;
  padding: 8px 12px;
  color: #f8fafc;
}

.markdown-body th {
  background: #334155;
  font-weight: 600;
}

.markdown-body a {
  color: #22c55e;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.markdown-body img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

.markdown-body hr {
  border: none;
  border-top: 1px solid #334155;
  margin: 24px 0;
}
</style>
