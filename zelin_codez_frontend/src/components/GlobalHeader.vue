<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">zelinCodez</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col>
        <div class="user-login-status">
          <div v-if="userLoginStore.loginUser.id">
            <a-space>
              <a-dropdown placement="bottom" arrow>
                <a-avatar
                  :src="(userLoginStore.loginUser as API.UserQueryVO).userAvatar"
                  size="large"
                />
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="toPersonalCenter">
                      个人中心
                      <UserOutlined />
                    </a-menu-item>
                    <a-menu-item @click="toSettings">
                      其他设置
                      <UserOutlined />
                    </a-menu-item>
                    <a-menu-item @click="logout">
                      登出
                      <LogoutOutlined />
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
              <div style="font-size: 17px; color: #00c2ff">
                {{ userLoginStore.loginUser.userName ?? '未登录' }}
              </div>
            </a-space>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message, type MenuProps } from 'ant-design-vue'
import { useUserLoginStore } from '@/stores/UserLoginStore'
import { LogoutOutlined, UserOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController'

const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path]
})
//引入全局登录登录状态
const userLoginStore = useUserLoginStore()
// 菜单配置项
const menuItems = ref([
  {
    key: '/',
    label: '首页',
    title: '首页',
  },
  {
    key: '/about',
    label: '关于',
    title: '关于我们',
  },
  {
    key: '/admin/user/manage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://github.com/wudirr/zelin_codez', target: '_blank' }, '赞助我们'),
    title: '赞助我们',
  },
])

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}
//跳转到个人中心
const toPersonalCenter = () => {
  message.warning('个人中心功能正在开发中，敬请期待！')
}
//跳转到设置页面
const toSettings = () => {
  message.warning('设置功能正在开发中，敬请期待！')
}
//登出功能
const logout = async () => {
  const res = await userLogout()
  if (res.code === 200 && res.data) {
    userLoginStore.setLoginUser({
      userName: '未登录',
    })
    message.success('登出成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  }
}
</script>

<style scoped>
.header {
  background: transparent !important;
  padding: 0 24px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  height: 48px;
  width: 48px;
}

.site-title {
  margin: 0;
  font-size: 18px;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

:deep(.ant-menu) {
  background: transparent !important;
}

:deep(.ant-menu-item) {
  color: rgba(255, 255, 255, 0.85) !important;
}

:deep(.ant-menu-item:hover) {
  color: #fff !important;
}

:deep(.ant-menu-item-selected) {
  color: #fff !important;
}

:deep(.ant-menu-item-selected)::after {
  border-bottom-color: #fff !important;
}

:deep(.ant-menu-dark .ant-menu-inline.ant-menu-sub) {
  background: rgba(0, 0, 0, 0.3) !important;
}

.user-login-status {
  color: #fff;
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%) !important;
}

:deep(.ant-dropdown-menu) {
  background: rgba(30, 41, 59, 0.95) !important;
  backdrop-filter: blur(12px);
}

:deep(.ant-dropdown-menu-item) {
  color: rgba(255, 255, 255, 0.85) !important;
}

:deep(.ant-dropdown-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
}

.ant-menu-horizontal {
  border-bottom: none !important;
}
</style>
