import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getMe } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLogin = computed(() => !!token.value)
  const role = computed(() => user.value?.role || '')
  const isMerchant = computed(() => role.value === 'MERCHANT')
  const isAdmin = computed(() => role.value === 'ADMIN')
  const isCustomer = computed(() => role.value === 'CUSTOMER' || !role.value)

  function setAuth(t, u) {
    token.value = t || ''
    user.value = u || null
    if (t) localStorage.setItem('token', t)
    else localStorage.removeItem('token')
    if (u) localStorage.setItem('user', JSON.stringify(u))
    else localStorage.removeItem('user')
  }

  async function login(form) {
    const res = await loginApi(form)
    const data = res.data || {}
    // 后端返回 { token, user } 或纯 token 字符串
    if (typeof data === 'string') {
      setAuth(data, null)
      await fetchMe()
    } else {
      setAuth(data.token, data.user)
      if (!data.user) await fetchMe()
    }
    return res
  }

  async function fetchMe() {
    if (!token.value) return null
    try {
      const res = await getMe()
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
      return res.data
    } catch {
      return null
    }
  }

  function logout() {
    setAuth('', null)
  }

  return {
    token, user, isLogin, role, isMerchant, isAdmin, isCustomer,
    setAuth, login, fetchMe, logout
  }
})
