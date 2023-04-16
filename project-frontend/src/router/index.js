import { createRouter, createWebHistory } from 'vue-router'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      //路由1：默认路径
      //第一层路由，展示WelcomeViews.vue
      path: '/',
      name: 'welcome',
      component: ()=>import('@/views/WelcomeViews.vue'),
      //第二层子路由：据路由不同，WelcomeViews.vue右侧展示的组件不同
      children: [
        {
          //path什么都不写，默认展示登录界面的组件
          path: '',
          name: 'welcome-login',
          component: ()=>import('@/components/welcome/LoginPage.vue')
        },{
          //路由到注册页面的组件
          path: 'register',
          name: 'welcome-register',
          component: ()=>import('@/components/welcome/RegisterPage.vue')
        },{
          //路由到忘记密码的组件
          path: 'forget',
          name: 'welcome-forget',
          component: ()=>import('@/components/welcome/ForgetPawPage.vue')
        }
      ]
    },
    //路由2：登录成功后的界面
    {
      path: '/index',
      name: 'index',
      component:()=>import('@/views/indexView.vue')
    }

  ]
})

export default router
