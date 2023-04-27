<template>
  <div>
    欢迎进入
  </div>
  <div>
    <el-button @click="logout()" type="danger" plain>退出登录</el-button>
  </div>

</template>

<script setup>
import {get} from "@/net"
import {ElMessage} from "element-plus";
import router from "@/router";
import {useStore} from "@/stores";
const store = useStore()//用户信息存储在这个全局变量中
const logout = ()=>{
  //向后端发送对应路径的get请求
  get('/api/auth/logout',(message) =>{//退出登录成功
    //先把用户的登录状态清空，才能成功返回到登录页面（配置了路由守卫，如果不清空，前端还是认为你是登录状态，回不到登录页面）
    ElMessage.success(message)
    store.auth.user = null
    router.push('/')//跳转回登录页面
  })
}
</script>

<style scoped>

</style>