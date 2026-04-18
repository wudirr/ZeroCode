import router from './router'
import { useUserLoginStore } from './stores/UserLoginStore'
import { message } from 'ant-design-vue'

//是否是第一次获取登录用户
let firstFetchLongUser = true

router.beforeEach(async (to, from, next) => {
  const userLoginStore = useUserLoginStore()
  if (firstFetchLongUser) {
    //获取登录用户
    await userLoginStore.fetchLoginUser()
    firstFetchLongUser = false
  }
  //检查权限
  const userRole = userLoginStore.loginUser.userRole
  const path = to.fullPath
  if (path.startsWith('/admin')) {
    if (userRole === 'admin') {
      next()
      return
    }
    //跳转到403界面
    next('/noauth')
    return
  }
  //如果没有登录,跳转到登录页面
  if (!userLoginStore.loginUser.id && !to.meta?.notLogin) {
    next('/user/login')
    return
  }
  next()
})
