import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
    config.headers.token = userStore.token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 统一后端 ResultJSON：{ code, msg, data }
    if (res && typeof res.code === 'number') {
      if (res.code === 200) {
        return res
      }
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        ElMessage.error(res.msg || '请先登录')
        router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
        return Promise.reject(res)
      }
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    if (status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      ElMessage.error(data?.msg || '请先登录')
      router.push({ path: '/login' })
    } else {
      ElMessage.error(data?.msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
