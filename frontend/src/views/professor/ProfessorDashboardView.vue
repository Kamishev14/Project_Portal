<template>
  <div>
    <h1 class="text-2xl font-bold tracking-tight text-white mb-8">Dashboard</h1>

    <BaseSpinner v-if="!stats" />

    <template v-else>
      <!-- Stats -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-10">
        <div class="stat-card">
          <p class="stat-number text-white">{{ stats.totalProjects }}</p>
          <p class="stat-label">Total</p>
        </div>
        <div class="stat-card">
          <p class="stat-number" style="color:#f59e0b">{{ stats.pending }}</p>
          <p class="stat-label">Pending</p>
        </div>
        <div class="stat-card">
          <p class="stat-number" style="color:#22c55e">{{ stats.approved }}</p>
          <p class="stat-label">Approved</p>
        </div>
        <div class="stat-card">
          <p class="stat-number" style="color:#f87171">{{ stats.declined }}</p>
          <p class="stat-label">Declined</p>
        </div>
      </div>

      <!-- Pending queue -->
      <div class="flex items-center justify-between mb-4">
        <p class="text-xs font-medium tracking-widest uppercase" style="color:#555">Pending Review</p>
        <RouterLink to="/professor/projects?status=PENDING" class="text-xs hover:underline" style="color:#22c55e">
          View all →
        </RouterLink>
      </div>

      <BaseSpinner v-if="store.loading" />

      <div v-else-if="pendingProjects.length === 0"
           class="rounded-lg py-8 text-center text-sm" style="border:1px dashed #222;color:#444">
        All caught up — no pending projects
      </div>

      <div v-else class="space-y-2">
        <RouterLink
          v-for="p in pendingProjects"
          :key="p.id"
          :to="`/professor/projects/${p.id}`"
          class="project-row"
        >
          <div class="flex items-center justify-between gap-4">
            <div class="min-w-0">
              <div class="flex items-center gap-2">
                <span class="text-xs font-mono" style="color:#444">#{{ p.queueNumber }}</span>
                <span class="font-medium text-white text-sm truncate">{{ p.title }}</span>
              </div>
              <div class="flex items-center gap-1.5 mt-0.5">
                <span class="text-xs font-mono font-bold" style="color:#22c55e">{{ p.student.studentIndex }}</span>
                <span class="text-xs" style="color:#444">· {{ p.student.name }} · {{ formatDate(p.createdAt) }}</span>
              </div>
            </div>
            <span class="badge-pending shrink-0">Review →</span>
          </div>
        </RouterLink>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useProjectStore } from '@/stores/projects'
import BaseSpinner from '@/components/common/BaseSpinner.vue'

const store = useProjectStore()
const stats = computed(() => store.stats)
const pendingProjects = computed(() =>
  store.allProjects.filter(p => p.status === 'PENDING').slice(0, 5)
)

onMounted(async () => {
  await store.fetchStats()
  await store.fetchAllProjects({ status: 'PENDING', size: 5 })
})

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })
}
</script>

<style scoped>
.stat-card {
  padding: 20px 16px;
  border-radius: 8px;
  background: #141414;
  border: 1px solid #222;
  text-align: center;
}
.stat-number { font-size: 1.875rem; font-weight: 700; line-height: 1; }
.stat-label  { font-size: 0.75rem; color: #555; margin-top: 6px; }

.project-row {
  display: block;
  padding: 12px 14px;
  border-radius: 6px;
  background: #141414;
  border: 1px solid #222;
  text-decoration: none;
  transition: border-color 0.15s, background 0.15s;
}
.project-row:hover { background: #181818; border-color: #2e2e2e; }
</style>
