<template>
  <div>
    <h1 class="text-2xl font-bold tracking-tight text-white mb-8">Dashboard</h1>

    <BaseSpinner v-if="!stats" />

    <template v-else>
      <!-- Settings -->
      <div class="card mb-10">
        <p class="text-xs font-medium tracking-widest uppercase mb-4" style="color:#555">Submission Settings</p>
        <div class="space-y-4">
          <div>
            <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Submission Deadline</label>
            <div class="flex gap-2">
              <input
                v-model="deadlineForm"
                type="datetime-local"
                class="input flex-1"
              />
              <button
                @click="saveDeadline"
                class="btn-primary text-xs"
                :disabled="store.loading"
              >
                <span v-if="store.loading">↻ </span>Save
              </button>
              <button
                v-if="store.submissionDeadline"
                @click="clearDeadline"
                class="btn-secondary text-xs"
                :disabled="store.loading"
              >
                Clear
              </button>
            </div>
            <p v-if="store.submissionDeadline" class="text-xs mt-2" style="color:#22c55e">
              Current: {{ formatDateTime(store.submissionDeadline) }}
            </p>
            <p v-else class="text-xs mt-2 italic" style="color:#555">
              No deadline set. Students can submit anytime.
            </p>
          </div>
        </div>
      </div>

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
import { computed, onMounted, ref, watch } from 'vue'
import { useProjectStore } from '@/stores/projects'
import { useToastStore } from '@/stores/toast'
import BaseSpinner from '@/components/common/BaseSpinner.vue'

const store = useProjectStore()
const toast = useToastStore()
const deadlineForm = ref('')

const stats = computed(() => store.stats)
const pendingProjects = computed(() =>
  store.allProjects.filter(p => p.status === 'PENDING').slice(0, 5)
)

// Sync deadline from store to form
watch(() => store.submissionDeadline, (newDeadline) => {
  if (newDeadline) {
    deadlineForm.value = formatDateTimeForInput(newDeadline)
  } else {
    deadlineForm.value = ''
  }
})

onMounted(async () => {
  await store.fetchStats()
  await store.fetchAllProjects({ status: 'PENDING', size: 5 })
  await store.fetchSubmissionDeadline()
})

async function saveDeadline() {
  if (!deadlineForm.value) {
    toast.error('Please select a deadline')
    return
  }
  try {
    const deadline = new Date(deadlineForm.value).toISOString()
    await store.updateSubmissionDeadline(deadline)
    toast.success('Deadline updated successfully')
  } catch (e) {
    toast.error('Failed to update deadline')
  }
}

async function clearDeadline() {
  try {
    await store.updateSubmissionDeadline(null)
    deadlineForm.value = ''
    toast.success('Deadline cleared')
  } catch (e) {
    toast.error('Failed to clear deadline')
  }
}

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })
}

function formatDateTime(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('en-GB', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatDateTimeForInput(date) {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
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
