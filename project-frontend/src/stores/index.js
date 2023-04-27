import {ref, computed, reactive} from 'vue'
import { defineStore } from 'pinia'
//把获取到的用户信息存储为全局变量，需要用的时候就很方便
export const useStore = defineStore('store', () => {
  const auth = reactive({
    user: null
  })

  return { auth }
})

