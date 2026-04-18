import { getLoginUser } from '@/api/userController'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserLoginStore = defineStore('userLogin', () => {
  const loginUser = ref<API.UserLoginVO>({
    userName: '未登录',
  })

  //获取登录用户信息
  async function fetchLoginUser() {
    const res = await getLoginUser()
    if (res.code === 200 && res.data) {
      loginUser.value = res.data
    }
  }

  //设置登录用户信息
  async function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return {
    loginUser,
    fetchLoginUser,
    setLoginUser,
  }
})
