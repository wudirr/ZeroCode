<template>
  <div id="userLoginPage">
    <div class="title">zelin_codez注册页面</div>
    <div class="desc">不写一行代码,生成完整应用</div>
    <a-form :model="formState" name="basic" @finish="onFinish" class="form">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账户' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账户" />
      </a-form-item>

      <a-form-item name="userPassword" :rules="[{ required: true, message: '请输入密码' }]">
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>

      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请再次输入密码' },
          { validator: validateCheckPassword, trigger: 'change' },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请再次输入密码" />
      </a-form-item>
      <div class="tips">
        已有账号?
        <RouterLink to="/user/login"> 去登录</RouterLink>
      </div>
      <a-form-item class="submit_button">
        <a-button type="primary" html-type="submit" style="width: 80%">提交</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { loginUser, registerUser } from '@/api/userController'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})
const router = useRouter()
const onFinish = async (values: any) => {
  const res = await registerUser(values)
  if (res.code === 200 && res.data) {
    //跳转到主页
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error(res.message || '注册失败')
  }
}
const validateCheckPassword = (rule: any, value: string) => {
  if (value !== '' && value !== formState.userPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}
</script>

<style scoped>
#userLoginPage {
  min-height: 66vh;
  max-width: 300px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  padding: 40px 16px;
}
.title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  text-align: center;
}
.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}
.form {
  width: 287px;
}
.tips {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}
.submit_button {
  text-align: center;
}
</style>
