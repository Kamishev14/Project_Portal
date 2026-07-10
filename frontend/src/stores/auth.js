import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const user  = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || null)

  const isLoggedIn  = computed(() => !!token.value)
  const isProfessor = computed(() => user.value?.role === 'PROFESSOR')
  const isStudent   = computed(() => user.value?.role === 'STUDENT')

  async function login(credentials) {
    const { data } = await authApi.login(credentials)
    _persist(data)
  }

  async function register(payload) {
    const { data } = await authApi.register(payload)
    _persist(data)
  }

  function logout() {
    user.value  = null
    token.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  function _persist(data) {
    token.value = data.token
    user.value  = {
      name:         data.name,
      email:        data.email,
      role:         data.role,
      userId:       data.userId,
      studentIndex: data.studentIndex  // null for professors
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  return { user, token, isLoggedIn, isProfessor, isStudent, login, register, logout }
})
