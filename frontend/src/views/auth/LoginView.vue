<template>
  <div class="min-h-screen flex items-center justify-center px-4" style="background:#0d0d0d">
    <div class="w-full max-w-sm">

      <div class="mb-10">
        <p class="text-xs font-semibold tracking-widest uppercase mb-3" style="color:#22c55e">ФИНКИ</p>
        <h1 class="text-3xl font-bold tracking-tight text-white">Проекти Хаб</h1>
        <p class="mt-2 text-sm" style="color:#666">Систем за поднесување проекти</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Email</label>
          <input v-model="form.email" type="email" class="input" placeholder="you@students.finki.ukim.mk" required />
        </div>
        <div>
          <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Password</label>
          <input v-model="form.password" type="password" class="input" placeholder="••••••••" required />
        </div>

        <button type="submit" class="btn-primary w-full mt-2" :disabled="loading">
          <span v-if="loading" class="mr-2 opacity-70">↻</span>
          Sign in
        </button>
      </form>

      <p class="mt-6 text-center text-sm" style="color:#555">
        No account?
        <RouterLink to="/register" class="font-medium hover:underline" style="color:#22c55e">Register</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'

const auth   = useAuthStore()
const toast   = useToastStore()
const router = useRouter()
const form   = ref({ email: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    await auth.login(form.value)
    toast.success('Logged in successfully')
    router.push(auth.isProfessor ? '/professor' : '/student')
  } catch (e) {
    toast.error(e.response?.data?.message || 'Invalid email or password')
  } finally {
    loading.value = false
  }
}
</script>
