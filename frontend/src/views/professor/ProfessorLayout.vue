<template>
  <div class="min-h-screen flex" style="background:#0d0d0d">

    <aside class="w-60 flex flex-col shrink-0" style="background:#0d0d0d;border-right:1px solid #1e1e1e">
      <div class="px-5 pt-6 pb-4">
        <p class="text-xs font-bold tracking-widest uppercase mb-0.5" style="color:#22c55e">ФИНКИ</p>
        <p class="text-sm font-semibold text-white">Проекти Хаб</p>
        <p class="text-xs mt-0.5" style="color:#444">Professor Portal</p>
      </div>

      <div class="mx-4 mb-3" style="height:1px;background:#1e1e1e"></div>

      <nav class="flex-1 px-3 space-y-0.5">
        <RouterLink to="/professor/dashboard" class="nav-link" active-class="nav-link-active">
          <span class="nav-icon">◈</span> Dashboard
        </RouterLink>
        <RouterLink to="/professor/projects" class="nav-link" active-class="nav-link-active">
          <span class="nav-icon">▤</span> All Projects
        </RouterLink>
      </nav>

      <div class="px-4 py-4" style="border-top:1px solid #1e1e1e">
        <p class="text-sm font-semibold text-white truncate">{{ auth.user?.name }}</p>
        <p class="text-xs truncate mt-0.5" style="color:#444">{{ auth.user?.email }}</p>
        <button @click="handleLogout" class="btn-secondary w-full mt-3 text-xs">Sign out</button>
      </div>
    </aside>

    <main class="flex-1 overflow-y-auto" style="background:#0d0d0d">
      <div class="max-w-5xl mx-auto px-8 py-8">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const auth   = useAuthStore()
const router = useRouter()
function handleLogout() { auth.logout(); router.push('/login') }
</script>

<style scoped>
.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  font-size: 0.8125rem;
  color: #666;
  border-radius: 5px;
  transition: all 0.1s;
  text-decoration: none;
}
.nav-link:hover { color: #ccc; background: #161616; }
.nav-link-active { color: #e5e5e5 !important; background: #1a1a1a !important; }
.nav-icon { font-size: 0.75rem; width: 14px; text-align: center; }
</style>
