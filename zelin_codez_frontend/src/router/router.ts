import UserLoginPage from '@/components/UserLoginPage.vue'
import UserManagerPage from '@/components/UserManagerPage.vue'
import UserRegisterPage from '@/components/UserRegisterPage.vue'
import HomeView from '@/views/HomeView.vue'

export const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
  },
  {
    path: '/user/login',
    name: 'login',
    component: UserLoginPage,
  },
  {
    path: '/user/register',
    name: 'register',
    component: UserRegisterPage,
  },
  {
    path: '/admin/user/manage',
    name: 'userManage',
    component: UserManagerPage,
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue'),
  },
]
