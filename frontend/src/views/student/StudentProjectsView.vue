<template>
  <div>
    <div class="flex items-start justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-white">My Projects</h1>
        <p class="text-sm mt-1" style="color:#555">
          Index <span class="font-mono font-semibold" style="color:#22c55e">{{ auth.user?.studentIndex }}</span>
        </p>
      </div>
      <RouterLink to="/student/projects/submit" class="btn-primary">+ Submit Idea</RouterLink>
    </div>

    <BaseSpinner v-if="store.loading" />

    <div v-else-if="store.myProjects.length === 0"
         class="rounded-lg py-16 text-center" style="border:1px dashed #2a2a2a">
      <p class="text-3xl mb-3">📭</p>
      <p class="font-medium text-white">No projects yet</p>
      <p class="text-sm mt-1 mb-5" style="color:#555">Submit your first idea to get started</p>
      <RouterLink to="/student/projects/submit" class="btn-primary">Submit an Idea</RouterLink>
    </div>

    <div v-else class="space-y-3">
      <RouterLink
        v-for="project in store.myProjects"
        :key="project.id"
        :to="`/student/projects/${project.id}`"
        class="project-row"
      >
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1.5">
              <span class="text-xs font-mono" style="color:#444">#{{ project.queueNumber }}</span>
              <StatusBadge :status="project.status" />
            </div>
            <p class="font-semibold text-white truncate">{{ project.title }}</p>
            <p class="text-sm mt-1 line-clamp-1" style="color:#666">{{ project.description }}</p>
            <p v-if="project.preferredProfessor || project.participants?.length" class="text-xs mt-2" style="color:#444">
              <span v-if="project.preferredProfessor">Preferred professor: {{ project.preferredProfessor.name }}</span>
              <span v-if="project.preferredProfessor && project.participants?.length"> · </span>
              <span v-if="project.participants?.length">{{ project.participants.length }} colleague{{ project.participants.length !== 1 ? 's' : '' }}</span>
            </p>
          </div>
          <div class="text-right shrink-0">
            <p class="text-xs" style="color:#444">{{ formatDate(project.createdAt) }}</p>
            <p v-if="project.reviews?.length" class="text-xs mt-1" style="color:#22c55e">
              {{ project.reviews.length }} review{{ project.reviews.length > 1 ? 's' : '' }}
            </p>
          </div>
        </div>
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useProjectStore } from '@/stores/projects'
import { useAuthStore } from '@/stores/auth'
import StatusBadge from '@/components/common/StatusBadge.vue'
import BaseSpinner from '@/components/common/BaseSpinner.vue'

const store = useProjectStore()
const auth  = useAuthStore()
onMounted(() => store.fetchMyProjects())

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
}
</script>

<style scoped>
.project-row {
  display: block;
  padding: 16px 18px;
  border-radius: 8px;
  background: #141414;
  border: 1px solid #222;
  text-decoration: none;
  transition: border-color 0.15s, background 0.15s;
}
.project-row:hover {
  background: #181818;
  border-color: #2e2e2e;
}
</style>
