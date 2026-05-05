import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import App from './App.vue'
import AiChatPlugin from '@linloop/ai-chat-plugin'
import router from './router'

const app = createApp(App)
app.use(Antd)
app.use(createPinia())
app.use(router)
app.use(AiChatPlugin)

app.mount('#app')
