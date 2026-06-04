import { ref, onUnmounted } from 'vue'

/**
 * 选中元素信息
 */
export interface ElementInfo {
  tagName: string
  id?: string
  className?: string
  textContent?: string
  attributes?: Record<string, string>
  computedStyles?: {
    display: string
    color: string
    backgroundColor: string
    fontSize: string
    width: string
    height: string
  }
  selector: string
  outerHTMLSnippet: string
}

/** 注入到 iframe 中的样式 ID */
const EDITOR_STYLE_ID = '__visual_editor_styles__'
/** 注入到 iframe 中的脚本标记属性 */
const EDITOR_SCRIPT_ATTR = 'data-visual-editor'
/** postMessage 来源过滤 */
const MESSAGE_TYPE = {
  ELEMENT_SELECTED: 'ELEMENT_SELECTED',
  CLEANUP: 'CLEANUP',
}

/**
 * 可视化编辑器核心逻辑
 */
export function useVisualEditor() {
  const isEditMode = ref(false)
  const selectedElements = ref<ElementInfo[]>([])
  const iframeEl = ref<HTMLIFrameElement | null>(null)

  // ==================== iframe 脚本注入 ====================

  /**
   * 生成要在 iframe 中注入的 CSS
   */
  function getEditorCSS(): string {
    return `
      .__ve-hover {
        outline: 2px dashed #3b82f6 !important;
        outline-offset: 2px !important;
        cursor: crosshair !important;
        transition: outline 0.15s ease !important;
      }
      .__ve-selected {
        outline: 3px solid #2563eb !important;
        outline-offset: 2px !important;
        background-color: rgba(59, 130, 246, 0.06) !important;
      }
    `
  }

  /**
   * 生成要在 iframe 中注入的 JS（作为字符串，在 iframe 内执行）
   */
  function getEditorScript(): string {
    return `
      (function() {
        // 防止重复注入
        if (window.__veInjected) return;
        window.__veInjected = true;
        // 全局开关：主网站通过设置此标记来控制编辑模式
        window.__veEditMode = true;

        var lastHoveredEl = null;

        function isExcluded(el) {
          if (!el || !el.tagName) return true;
          var tag = el.tagName.toLowerCase();
          return tag === 'html' || tag === 'body' || tag === 'head' || tag === 'script' || tag === 'style' || tag === 'meta' || tag === 'link';
        }

        function getSelector(el) {
          if (!el || isExcluded(el)) return '';
          var parts = [];
          while (el && el.nodeType === 1 && !isExcluded(el)) {
            var seg = el.tagName.toLowerCase();
            if (el.id) {
              seg += '#' + el.id;
              parts.unshift(seg);
              break;
            } else {
              var siblings = el.parentNode ? Array.from(el.parentNode.children).filter(function(c) { return c.tagName === el.tagName; }) : [];
              if (siblings.length > 1) {
                var idx = siblings.indexOf(el) + 1;
                seg += ':nth-of-type(' + idx + ')';
              }
              parts.unshift(seg);
            }
            el = el.parentNode;
          }
          return parts.join(' > ');
        }

        function getAttributes(el) {
          var attrs = {};
          var usefulAttrs = ['href', 'src', 'alt', 'placeholder', 'type', 'name', 'value', 'title', 'role', 'for', 'action', 'method', 'target'];
          for (var i = 0; i < el.attributes.length; i++) {
            var attr = el.attributes[i];
            if (usefulAttrs.indexOf(attr.name) !== -1) {
              attrs[attr.name] = attr.value;
            }
          }
          return attrs;
        }

        function getComputedStyles(el) {
          try {
            var cs = window.getComputedStyle(el);
            return {
              display: cs.display || '',
              color: cs.color || '',
              backgroundColor: cs.backgroundColor || '',
              fontSize: cs.fontSize || '',
              width: cs.width || '',
              height: cs.height || ''
            };
          } catch(e) {
            return { display: '', color: '', backgroundColor: '', fontSize: '', width: '', height: '' };
          }
        }

        function getElementInfo(el) {
          var text = el.textContent || '';
          if (text.length > 100) text = text.substring(0, 100) + '...';
          var html = el.outerHTML || '';
          if (html.length > 200) html = html.substring(0, 200) + '...';
          return {
            tagName: el.tagName.toLowerCase(),
            id: el.id || undefined,
            className: el.className && typeof el.className === 'string' ? el.className : undefined,
            textContent: text || undefined,
            attributes: getAttributes(el),
            computedStyles: getComputedStyles(el),
            selector: getSelector(el),
            outerHTMLSnippet: html
          };
        }

        // 悬浮高亮
        document.addEventListener('mouseover', function(e) {
          if (!window.__veEditMode) return;
          var el = e.target;
          if (isExcluded(el)) return;
          if (lastHoveredEl && lastHoveredEl !== el) {
            lastHoveredEl.classList.remove('__ve-hover');
          }
          if (!el.classList.contains('__ve-selected')) {
            el.classList.add('__ve-hover');
          }
          lastHoveredEl = el;
        }, true);

        document.addEventListener('mouseout', function(e) {
          if (!window.__veEditMode) return;
          var el = e.target;
          if (el && el.classList) {
            el.classList.remove('__ve-hover');
          }
        }, true);

        // 点击选中
        document.addEventListener('click', function(e) {
          if (!window.__veEditMode) return;
          var el = e.target;
          if (isExcluded(el)) return;
          e.preventDefault();
          e.stopPropagation();

          // 切换选中状态
          if (el.classList.contains('__ve-selected')) {
            el.classList.remove('__ve-selected');
          } else {
            el.classList.add('__ve-selected');
            el.classList.remove('__ve-hover');
            // 发送元素信息给主网站
            var info = getElementInfo(el);
            window.parent.postMessage({
              type: '${MESSAGE_TYPE.ELEMENT_SELECTED}',
              payload: info
            }, '*');
          }
        }, true);
      })();
    `
  }

  /**
   * 向 iframe 注入编辑器脚本和样式
   */
  function injectEditorScript() {
    const iframe = iframeEl.value
    if (!iframe) return

    try {
      const doc = iframe.contentDocument
      if (!doc || !doc.body) return

      const win = iframe.contentWindow
      if (!win) return

      // 如果已经注入过，只需重新打开开关即可
      if ((win as any).__veInjected) {
        ;(win as any).__veEditMode = true
        // 重新注入样式
        let styleEl = doc.getElementById(EDITOR_STYLE_ID) as HTMLStyleElement | null
        if (!styleEl) {
          styleEl = doc.createElement('style')
          styleEl.id = EDITOR_STYLE_ID
          doc.head.appendChild(styleEl)
        }
        styleEl.textContent = getEditorCSS()
        return
      }

      // 注入样式
      let styleEl = doc.getElementById(EDITOR_STYLE_ID) as HTMLStyleElement | null
      if (!styleEl) {
        styleEl = doc.createElement('style')
        styleEl.id = EDITOR_STYLE_ID
        doc.head.appendChild(styleEl)
      }
      styleEl.textContent = getEditorCSS()

      // 注入脚本
      const scriptEl = doc.createElement('script')
      scriptEl.setAttribute(EDITOR_SCRIPT_ATTR, 'true')
      scriptEl.textContent = getEditorScript()
      doc.body.appendChild(scriptEl)
    } catch (err) {
      console.warn('[VisualEditor] 注入脚本失败:', err)
    }
  }

  /**
   * 清理 iframe 中的编辑器脚本和样式
   * 关键：通过设置 window.__veEditMode = false 来禁用事件监听器
   * （移除 <script> 标签并不能移除已注册的事件监听器）
   */
  function cleanupEditorScript() {
    const iframe = iframeEl.value
    if (!iframe) return

    try {
      const doc = iframe.contentDocument
      const win = iframe.contentWindow
      if (!doc || !win) return

      // 核心：关闭编辑模式开关，事件监听器会检查此标记并跳过
      try { (win as any).__veEditMode = false } catch (_) {}

      // 移除样式
      const styleEl = doc.getElementById(EDITOR_STYLE_ID)
      if (styleEl) styleEl.remove()

      // 移除所有高亮类
      const highlighted = doc.querySelectorAll('.__ve-hover, .__ve-selected')
      highlighted.forEach((el) => {
        el.classList.remove('__ve-hover', '__ve-selected')
      })
    } catch (err) {
      console.warn('[VisualEditor] 清理脚本失败:', err)
    }
  }

  // ==================== postMessage 通信 ====================

  function handleMessage(event: MessageEvent) {
    if (event.data?.type !== MESSAGE_TYPE.ELEMENT_SELECTED) return
    const info: ElementInfo = event.data.payload
    if (!info || !info.tagName) return

    // 检查是否已经选中过同一元素（通过 selector 去重）
    const exists = selectedElements.value.some(
      (el) => el.selector === info.selector && el.outerHTMLSnippet === info.outerHTMLSnippet
    )
    if (!exists) {
      selectedElements.value.push(info)
    }
  }

  // ==================== 公开方法 ====================

  /**
   * 设置 iframe DOM 引用（通过模板 ref 回调）
   */
  function setIframeRef(el: any) {
    iframeEl.value = el as HTMLIFrameElement | null
  }

  /**
   * 切换编辑模式
   */
  function toggleEditMode() {
    isEditMode.value = !isEditMode.value
    if (isEditMode.value) {
      // 进入编辑模式：注入脚本，开始监听消息
      injectEditorScript()
      window.addEventListener('message', handleMessage)
    } else {
      // 退出编辑模式：清理脚本，停止监听
      cleanupEditorScript()
      window.removeEventListener('message', handleMessage)
      clearSelection()
    }
  }

  /**
   * 移除指定索引的选中元素
   */
  function removeElement(index: number) {
    selectedElements.value.splice(index, 1)
  }

  /**
   * 清空所有选中元素
   */
  function clearSelection() {
    selectedElements.value = []
  }

  /**
   * 发送消息后清理
   */
  function cleanupAfterSend() {
    clearSelection()
    if (isEditMode.value) {
      isEditMode.value = false
      cleanupEditorScript()
      window.removeEventListener('message', handleMessage)
    }
  }

  /**
   * 将选中元素信息拼接到用户消息中
   */
  function enhanceMessage(userMessage: string): string {
    if (selectedElements.value.length === 0) return userMessage

    const parts: string[] = ['[可视化编辑 - 选中元素]']

    selectedElements.value.forEach((el, idx) => {
      parts.push(`元素${idx + 1}:`)
      parts.push(`  标签: <${el.tagName}>`)
      if (el.selector) parts.push(`  选择器: ${el.selector}`)
      if (el.id) parts.push(`  ID: #${el.id}`)
      if (el.className) parts.push(`  类名: ${el.className}`)
      if (el.textContent) parts.push(`  文本: "${el.textContent}"`)
      if (el.outerHTMLSnippet) parts.push(`  HTML片段: ${el.outerHTMLSnippet}`)
      if (el.attributes && Object.keys(el.attributes).length > 0) {
        const attrStr = Object.entries(el.attributes)
          .map(([k, v]) => `${k}="${v}"`)
          .join(' ')
        parts.push(`  属性: ${attrStr}`)
      }
      if (el.computedStyles) {
        const styles = el.computedStyles
        const usefulStyles: string[] = []
        if (styles.display) usefulStyles.push(`display:${styles.display}`)
        if (styles.color && styles.color !== 'rgb(0, 0, 0)') usefulStyles.push(`color:${styles.color}`)
        if (styles.backgroundColor && styles.backgroundColor !== 'rgba(0, 0, 0, 0)') usefulStyles.push(`background:${styles.backgroundColor}`)
        if (styles.fontSize) usefulStyles.push(`font-size:${styles.fontSize}`)
        if (styles.width) usefulStyles.push(`width:${styles.width}`)
        if (styles.height) usefulStyles.push(`height:${styles.height}`)
        if (usefulStyles.length > 0) parts.push(`  样式: ${usefulStyles.join('; ')}`)
      }
    })

    parts.push('')
    parts.push(`用户指令: ${userMessage}`)

    return parts.join('\n')
  }

  /**
   * 当 iframe 刷新后重新注入（供 watch previewVersion 调用）
   */
  function reInjectIfNeeded() {
    if (isEditMode.value) {
      // 等待 iframe 加载完成
      const iframe = iframeEl.value
      if (iframe) {
        const doInject = () => {
          injectEditorScript()
          iframe.removeEventListener('load', doInject)
        }
        if (iframe.contentDocument?.readyState === 'complete') {
          // 短暂延迟确保 DOM 就绪
          setTimeout(injectEditorScript, 100)
        } else {
          iframe.addEventListener('load', doInject)
        }
      }
    }
  }

  /**
   * 组件卸载时清理
   */
  function cleanup() {
    window.removeEventListener('message', handleMessage)
    cleanupEditorScript()
    selectedElements.value = []
    isEditMode.value = false
  }

  // 自动在组件卸载时清理
  onUnmounted(cleanup)

  return {
    isEditMode,
    selectedElements,
    setIframeRef,
    toggleEditMode,
    removeElement,
    clearSelection,
    cleanupAfterSend,
    enhanceMessage,
    reInjectIfNeeded,
    cleanup,
  }
}
