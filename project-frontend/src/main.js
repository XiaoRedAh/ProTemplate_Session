import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import 'element-plus/dist/index.css'
import axios from "axios";

const app = createApp(App)
//配置服务器地址（后端）前缀
axios.defaults.baseURL='http://localhost:8080'

app.use(createPinia())
app.use(router)

app.mount('#app')
