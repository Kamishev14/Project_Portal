<template>
  <div>
    <h1 class="text-2xl font-bold tracking-tight text-white mb-8">All Projects</h1>

    <!-- Index search -->
    <div class="card mb-6">
      <p class="text-xs font-medium tracking-widest uppercase mb-3" style="color:#555">Search by student index</p>
      <div class="flex gap-2">
        <input
          v-model="indexSearch"
          type="text"
          class="input font-mono w-32"
          placeholder="190042"
          maxlength="6"
          inputmode="numeric"
          @input="indexSearch = indexSearch.replace(/\D/g, '').slice(0, 6)"
          @keyup.enter="searchByIndex"
        />
        <button @click="searchByIndex" class="btn-primary" :disabled="indexSearch.length !== 6">Search</button>
        <button v-if="indexMode" @click="clearIndexSearch" class="btn-secondary">Clear</button>
      </div>

      <div v-if="indexMode" class="mt-4">
        <BaseSpinner v-if="indexLoading" />
        <p v-else-if="indexError" class="text-sm" style="color:#f87171">{{ indexError }}</p>
        <template v-else-if="indexResults.length > 0">
          <p class="text-xs mb-3" style="color:#555">
            {{ indexResults.length }} project{{ indexResults.length !== 1 ? 's' : '' }} for
            <span class="font-mono font-bold" style="color:#22c55e">{{ lastSearchedIndex }}</span>
            — {{ indexResults[0]?.student?.name }}
          </p>
          <div class="space-y-2">
            <RouterLink
              v-for="p in indexResults"
              :key="p.id"
              :to="`/professor/projects/${p.id}`"
              class="search-row"
            >
              <div class="flex items-center gap-2">
                <span class="text-xs font-mono" style="color:#444">#{{ p.queueNumber }}</span>
                <StatusBadge :status="p.status" />
                <span class="text-sm font-medium text-white truncate">{{ p.title }}</span>
              </div>
              <span class="text-xs shrink-0" style="color:#444">{{ formatDate(p.createdAt) }} →</span>
            </RouterLink>
          </div>
        </template>
        <p v-else class="text-sm" style="color:#444">No projects found.</p>
      </div>
    </div>

    <!-- Filters -->
    <div class="flex flex-wrap gap-2 mb-6">
      <button
        v-for="f in filters"
        :key="f.value"
        @click="setFilter(f.value)"
        class="filter-btn"
        :class="{ 'filter-btn-active': activeFilter === f.value }"
      >
        {{ f.label }}
      </button>
    </div>

    <BaseSpinner v-if="store.loading" />

    <template v-else>
      <div v-if="store.allProjects.length === 0"
           class="rounded-lg py-12 text-center text-sm" style="border:1px dashed #222;color:#444">
        No projects found.
      </div>

      <div v-else class="space-y-2">
        <RouterLink
          v-for="p in store.allProjects"
          :key="p.id"
          :to="`/professor/projects/${p.id}`"
          class="project-row"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-xs font-mono" style="color:#444">#{{ p.queueNumber }}</span>
                <StatusBadge :status="p.status" />
              </div>
              <p class="font-semibold text-white truncate">{{ p.title }}</p>
              <div class="flex items-center gap-2 mt-0.5">
                <span class="text-xs font-mono font-bold" style="color:#22c55e">{{ p.student.studentIndex }}</span>
                <span class="text-xs" style="color:#444">{{ p.student.name }} · {{ p.student.email }}</span>
              </div>
              <p v-if="p.preferredProfessor || p.participants?.length" class="text-xs mt-1" style="color:#444">
                <span v-if="p.preferredProfessor">Preferred professor: {{ p.preferredProfessor.name }}</span>
                <span v-if="p.preferredProfessor && p.participants?.length"> · </span>
                <span v-if="p.participants?.length">{{ p.participants.length }} teammate{{ p.participants.length !== 1 ? 's' : '' }}</span>
              </p>
            </div>
            <p class="text-xs shrink-0" style="color:#444">{{ formatDate(p.createdAt) }}</p>
          </div>
        </RouterLink>
      </div>

      <!-- Pagination -->
      <div v-if="store.pagination.totalPages > 1" class="flex justify-center gap-1.5 mt-6">
        <button
          v-for="page in store.pagination.totalPages"
          :key="page"
          @click="goToPage(page - 1)"
          class="page-btn"
          :class="{ 'page-btn-active': store.pagination.page === page - 1 }"
        >{{ page }}</button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/projects'
import { projectApi } from '@/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import BaseSpinner from '@/components/common/BaseSpinner.vue'

const store  = useProjectStore()
const route  = useRoute()
const router = useRouter()

const filters = [
  { label: 'All',         value: '' },
  { label: '⏳ Pending',   value: 'PENDING' },
  { label: '✅ Approved',  value: 'APPROVED' },
  { label: '❌ Declined',  value: 'DECLINED' },
  { label: '🔄 Revision',  value: 'REVISION_REQUESTED' }
]
const activeFilter = ref(route.query.status || '')

function setFilter(val) {
  activeFilter.value = val
  router.replace({ query: val ? { status: val } : {} })
  load(0)
}
function load(page = 0) {
  store.fetchAllProjects({ status: activeFilter.value || undefined, page, size: 10 })
}
function goToPage(page) { load(page) }

const indexSearch       = ref('')
const indexMode         = ref(false)
const indexLoading      = ref(false)
const indexError        = ref('')
const indexResults      = ref([])
const lastSearchedIndex = ref('')

async function searchByIndex() {
  if (indexSearch.value.length !== 6) return
  indexMode.value = true; indexLoading.value = true
  indexError.value = ''; indexResults.value = []
  lastSearchedIndex.value = indexSearch.value
  try {
    const res = await projectApi.getByStudentIndex(indexSearch.value)
    indexResults.value = res.data
  } catch (e) {
    indexError.value = e.response?.data?.message || 'Student not found'
  } finally {
    indexLoading.value = false
  }
}
function clearIndexSearch() {
  indexSearch.value = ''; indexMode.value = false
  indexError.value = ''; indexResults.value = []
}

onMounted(() => {
  if (route.query.searchIndex) { indexSearch.value = route.query.searchIndex; searchByIndex() }
  load()
})
watch(() => route.query.status, val => { activeFilter.value = val || ''; load(0) })

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
}
</script>

<style scoped>
.project-row, .search-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px; border-radius: 6px;
  background: #141414; border: 1px solid #222;
  text-decoration: none; transition: border-color 0.15s, background 0.15s;
}
.project-row { display: block; padding: 14px 16px; }
.project-row:hover, .search-row:hover { background: #181818; border-color: #2e2e2e; }

.filter-btn {
  padding: 5px 12px; font-size: 0.75rem; border-radius: 20px;
  border: 1px solid #2a2a2a; background: #141414; color: #666;
  cursor: pointer; transition: all 0.1s;
}
.filter-btn:hover { color: #ccc; border-color: #3a3a3a; }
.filter-btn-active { background: #22c55e; color: #0d0d0d; border-color: #22c55e; font-weight: 600; }

.page-btn {
  width: 30px; height: 30px; border-radius: 5px; font-size: 0.8rem;
  background: #141414; border: 1px solid #222; color: #666; cursor: pointer;
}
.page-btn:hover { border-color: #333; color: #ccc; }
.page-btn-active { background: #22c55e; color: #0d0d0d; border-color: #22c55e; font-weight: 700; }
</style>
