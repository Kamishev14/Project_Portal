<template>
  <div class="min-h-screen flex items-center justify-center px-4" style="background:#0d0d0d">
    <div class="w-full max-w-sm">

      <div class="mb-10">
        <p class="text-xs font-semibold tracking-widest uppercase mb-3" style="color:#22c55e">ФИНКИ</p>
        <h1 class="text-3xl font-bold tracking-tight text-white">Проекти Хаб</h1>
        <p class="mt-2 text-sm" style="color:#666">
          {{ isProfessorEmail ? 'Professor account' : 'Student account' }}
        </p>
      </div>

      <form @submit.prevent="handleRegister" class="space-y-4">
        <div>
          <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Full Name</label>
          <input v-model="form.name" type="text" class="input" placeholder="Jane Doe" required />
        </div>

        <div>
          <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Email</label>
          <input v-model="form.email" type="email" class="input" placeholder="you@students.finki.ukim.mk" required />
          <p v-if="isProfessorEmail" class="text-xs mt-1.5" style="color:#22c55e">Professor account detected</p>
          <p v-else-if="isStudentEmail" class="text-xs mt-1.5" style="color:#22c55e">Student account detected</p>
          <p v-else-if="form.email.includes('@') && !isValidDomain" class="text-xs mt-1.5" style="color:#f87171">
            Only @finki.ukim.mk or @students.finki.ukim.mk emails allowed
          </p>
        </div>

        <!-- Student index — only for student emails -->
        <div v-if="isStudentEmail">
          <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Student Index</label>
          <input
            v-model="form.studentIndex"
            type="text"
            class="input font-mono tracking-widest"
            placeholder="190042"
            maxlength="6"
            inputmode="numeric"
            @input="form.studentIndex = form.studentIndex.replace(/\D/g, '').slice(0, 6)"
          />
          <p v-if="form.studentIndex.length > 0 && form.studentIndex.length < 6"
             class="text-xs mt-1.5" style="color:#f59e0b">
            {{ 6 - form.studentIndex.length }} more digit{{ 6 - form.studentIndex.length !== 1 ? 's' : '' }}
          </p>
          <p v-if="form.studentIndex.length === 6" class="text-xs mt-1.5" style="color:#555">
            Enrolled {{ enrollmentYear }}
          </p>
        </div>

        <div>
          <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Password</label>
          <input v-model="form.password" type="password" class="input" placeholder="Min. 6 characters" required minlength="6" />
        </div>

        <button type="submit" class="btn-primary w-full mt-2" :disabled="loading || !canSubmit">
          <span v-if="loading" class="mr-2 opacity-70">↻</span>
          Create Account
        </button>
      </form>

      <p class="mt-6 text-center text-sm" style="color:#555">
        Already registered?
        <RouterLink to="/login" class="font-medium hover:underline" style="color:#22c55e">Sign in</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'

const auth = useAuthStore()
const toast = useToastStore()
const router = useRouter()
const form = ref({ name: '', studentIndex: '', email: '', password: '' })
const loading = ref(false)

const isStudentEmail = computed(() => form.value.email.toLowerCase().endsWith('@students.finki.ukim.mk'))
const isProfessorEmail = computed(() =>
  form.value.email.toLowerCase().endsWith('@finki.ukim.mk') &&
  !form.value.email.toLowerCase().endsWith('@students.finki.ukim.mk')
)
const isValidDomain = computed(() => isStudentEmail.value || isProfessorEmail.value)

const canSubmit = computed(() => {
  if (!isValidDomain.value) return false
  return isStudentEmail.value ? form.value.studentIndex.length === 6 : true
})

const enrollmentYear = computed(() => {
  if (form.value.studentIndex.length < 2) return ''
  const prefix = parseInt(form.value.studentIndex.substring(0, 2))
  return prefix <= new Date().getFullYear() % 100 ? 2000 + prefix : 1900 + prefix
})

async function handleRegister() {
  loading.value = true
  try {
    await auth.register(form.value)
    toast.success('Account created successfully')
    router.push(auth.isProfessor ? '/professor' : '/student')
  } catch (e) {
    const data = e.response?.data
    const message = typeof data === 'object'
      ? Object.values(data).join(', ')
      : data?.message || 'Registration failed'
    toast.error(message)
  } finally {
    loading.value = false
  }
}
</script>
