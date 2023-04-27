<script setup>
import {get} from "@/net";
import {ElMessage} from "element-plus";
import {useStore} from "@/stores";
import router from "@/router";
//存储用户信息的全局变量
const store = useStore()

if(store.auth.user == null){//如果存储的用户信息为空，则请求获取用户信息
  get('/api/user/me',(message)=>{
    //如果获取成功，说明用户已登录。
    // 那么先将用户信息存储，然后自动跳转到index页面（不会再展示登录页面了）
    store.auth.user = message
    router.push('/index')
  },()=>{
    //如果获取失败，依然置为空
   store.auth.user = null
  })

}

</script>

<template>
 <router-view></router-view>
</template>

<style scoped>

</style>
