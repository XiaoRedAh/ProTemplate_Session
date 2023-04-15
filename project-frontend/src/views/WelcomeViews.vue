<template>
  <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
    <!--左侧壁纸固定-->
    <div style="flex:1">
<!--      <el-image style="width: 100%;height: 100%" fit ="cover"
                scr="../assets/img/login.png"/>-->
      <img style="width: 100%;height: 100%" fit ="cover" src="@/assets/img/login.png"/>
    </div>
    <div class="welcome-title">
      <div style="font-size: 30px;font-weight: bold">欢迎来到xiaoRed的网站</div>
      <div style="margin-top: 10px">水一段话啦啦啦啦啦啦</div>
      <div style="margin-top: 5px">再水一段话啦啦啦啦啦啦</div>
    </div>
    <!--右侧：根据路由展示登录。注册组件-->
    <div style="width: 450px;text-align: center;background-color: white;z-index: 1">
      <!--根据路由切换展示的组件-->
      <router-view v-slot = "{Component}">
        <!--给切换组件添加一个过渡动画-->
        <transition name="el-fade-in-linear">
          <component :is="Component" style="height: 100%"/>
        </transition>
      </router-view>
    </div>

  </div>
</template>

<script setup>

</script>

<style scoped>
.welcome-title{
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}
</style>