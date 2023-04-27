import { createRouter, createWebHistory } from 'vue-router'
import {useStore} from "@/stores";


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
//路由守卫
router.beforeEach((to, from, next)=>{
  const store = useStore()//用户的信息存储在这个全局变量里
  if(store.auth.user != null && to.name.startsWith('welcome-')){//用户已经登录且请求的页面的name以welcome-开头
    next('/index')//丢到index页面，不能回到关于欢迎的页面（登录，注册，重置密码）
  }else if(store.auth.user == null && to.fullPath.startsWith('/index')){//用户没有登录且请求的页面路径以index开头
    next('/')//不能让他访问，丢到登录页面
  }else if(to.matched.length === 0){//请求的页面不存在
    next('/index')//先丢到index页面。对于已登录的用户，就会展现index页面；对于没登录的用户，则会再被丢到登录页面
  }else{//其他情况该去哪去哪
    next()
  }
})

export default router
