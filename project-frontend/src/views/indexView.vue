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
const logout = ()=>{
  //向后端发送对应路径的get请求
  get('api/auth/logout',(message) =>{
    //退出登录成功，跳转到登录界面
    ElMessage.success(message)
    router.push('/')
  })
}
</script>

<style scoped>

</style>