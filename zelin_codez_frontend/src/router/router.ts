import NoAuthPage from '@/components/NoAuthPage.vue'
import UserLoginPage from '@/components/UserLoginPage.vue'
import UserManagerPage from '@/components/UserManagerPage.vue'
import AppManagerPage from '@/components/AppManagerPage.vue'
import UserRegisterPage from '@/components/UserRegisterPage.vue'
import HomeView from '@/views/HomeView.vue'
import ChatView from '@/views/ChatView.vue'
import AppEdit from '@/components/AppEdit.vue'

export const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: {
      notLogin: true,
    },
  },
  {
    path: '/user/login',
    name: 'login',
    component: UserLoginPage,
    meta: {
      notLogin: true,
    },
  },
  {
    path: '/user/register',
    name: 'register',
    component: UserRegisterPage,
    meta: {
      notLogin: true,
    },
  },
  {
    path: '/admin/user/manage',
    name: 'userManage',
    component: UserManagerPage,
  },
  {
    path: '/admin/app/manage',
    name: 'appManage',
    component: AppManagerPage,
  },
  {
    path: '/app/edit/:appId',
    name: 'appEdit',
    component: AppEdit,
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue'),
  },
  {
    path: '/chat/:appId',
    name: 'chat',
    component: ChatView,
    meta: {
      notLogin: true,
      hideHeader: true,
    },
  },
  {
    path: '/noauth',
    name: 'noauth',
    component: NoAuthPage,
    meta: {
      notLogin: true,
    },
  },
]
