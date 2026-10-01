import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useToastStore = defineStore('toast', () => {
  const toasts = ref([])

  function push(type, message) {
    const id = Date.now() + Math.random()
    toasts.value.push({ id, type, message })

    setTimeout(() => {
      toasts.value = toasts.value.filter(t => t.id !== id)
    }, 3000)
  }

  function success(message) {
    push('success', message)
  }

  function error(message) {
    push('error', message)
  }

  return { toasts, success, error }
})
