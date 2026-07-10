<template>
  <div>
    <RouterLink to="/student/projects" class="text-xs hover:underline" style="color:#555">← Back</RouterLink>

    <BaseSpinner v-if="store.loading && !project" />

    <template v-else-if="project">
      <div class="mt-5 mb-8">
        <div class="flex items-center gap-2 mb-2">
          <span class="text-xs font-mono" style="color:#444">Queue #{{ project.queueNumber }}</span>
          <StatusBadge :status="project.status" />
        </div>
        <div class="flex items-start justify-between gap-4">
          <h1 class="text-2xl font-bold tracking-tight text-white">{{ project.title }}</h1>
          <!-- Edit/Delete — only shown for editable statuses -->
          <div v-if="canEdit" class="flex gap-2 shrink-0">
            <button @click="startEdit" class="btn-secondary text-xs">Edit</button>
            <button @click="confirmDelete" class="btn-danger text-xs">Delete</button>
          </div>
        </div>
        <p class="text-xs mt-1.5" style="color:#444">Submitted {{ formatDate(project.createdAt) }}</p>
      </div>

      <!-- Delete confirmation -->
      <div v-if="showDeleteConfirm" class="card mb-5" style="border-color:#3a0000;background:#1a0000">
        <p class="text-sm font-medium text-white mb-1">Delete this project?</p>
        <p class="text-xs mb-4" style="color:#aaa">This cannot be undone.</p>
        <div class="flex gap-2">
          <button @click="handleDelete" class="btn-danger text-xs" :disabled="store.loading">
            <span v-if="store.loading">↻ </span>Yes, delete
          </button>
          <button @click="showDeleteConfirm = false" class="btn-secondary text-xs">Cancel</button>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
        <!-- Left -->
        <div class="md:col-span-2 space-y-4">

          <!-- Edit form -->
          <div v-if="editing" class="card">
            <p class="text-xs font-medium tracking-widest uppercase mb-4" style="color:#555">Edit Project</p>
            <form @submit.prevent="handleEdit" class="space-y-4">
              <div>
                <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Title</label>
                <input v-model="editForm.title" type="text" class="input" required minlength="5" maxlength="200" />
              </div>
              <div>
                <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Description</label>
                <textarea v-model="editForm.description" class="input resize-none" rows="5" required minlength="20"></textarea>
              </div>
              <div v-if="editError" class="px-3 py-2 rounded text-sm" style="background:#1a0000;color:#f87171;border:1px solid #3a0000">
                {{ editError }}
              </div>
              <div class="flex gap-2">
                <button type="submit" class="btn-primary text-xs" :disabled="store.loading">
                  <span v-if="store.loading">↻ </span>Save Changes
                </button>
                <button type="button" @click="cancelEdit" class="btn-secondary text-xs">Cancel</button>
              </div>
            </form>
          </div>

          <!-- Description (view mode) -->
          <div v-else class="card">
            <p class="text-xs font-medium tracking-widest uppercase mb-3" style="color:#555">Description</p>
            <p class="text-sm leading-relaxed whitespace-pre-wrap" style="color:#ccc">{{ project.description }}</p>
          </div>

          <!-- Review history -->
          <div v-if="project.reviews?.length" class="card">
            <p class="text-xs font-medium tracking-widest uppercase mb-4" style="color:#555">Review History</p>
            <div class="space-y-4">
              <div
                v-for="review in project.reviews"
                :key="review.id"
                class="pl-3 py-1"
                :style="`border-left: 2px solid ${reviewColor(review.status)}`"
              >
                <div class="flex items-center gap-2 mb-1">
                  <StatusBadge :status="review.status" />
                  <span class="text-xs" style="color:#444">{{ review.professorName }} · {{ formatDate(review.reviewedAt) }}</span>
                </div>
                <p v-if="review.comment" class="text-sm" style="color:#aaa">{{ review.comment }}</p>
                <p v-else class="text-xs italic" style="color:#444">No comment.</p>
              </div>
            </div>
          </div>

          <div v-else class="rounded-lg py-8 text-center text-sm" style="border:1px dashed #222;color:#444">
            Awaiting review
          </div>
        </div>

        <!-- Right -->
        <div>
          <QrCodeCard :project="project" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/projects'
import StatusBadge from '@/components/common/StatusBadge.vue'
import BaseSpinner from '@/components/common/BaseSpinner.vue'
import QrCodeCard from '@/components/common/QrCodeCard.vue'

const route   = useRoute()
const router  = useRouter()
const store   = useProjectStore()
const project = computed(() => store.currentProject)

// Edit state
const editing   = ref(false)
const editForm  = ref({ title: '', description: '' })
const editError = ref('')

// Delete state
const showDeleteConfirm = ref(false)

// Can only edit/delete if pending or revision requested
const canEdit = computed(() =>
  project.value &&
  ['PENDING', 'REVISION_REQUESTED'].includes(project.value.status)
)

onMounted(() => store.fetchMyProject(route.params.id))

function startEdit() {
  editForm.value = { title: project.value.title, description: project.value.description }
  editError.value = ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
  editError.value = ''
}

async function handleEdit() {
  editError.value = ''
  try {
    await store.editProject(route.params.id, editForm.value)
    editing.value = false
  } catch (e) {
    editError.value = store.error || 'Failed to save changes'
  }
}

function confirmDelete() {
  showDeleteConfirm.value = true
}

async function handleDelete() {
  try {
    await store.deleteProject(route.params.id)
    router.push('/student/projects')
  } catch (e) {
    showDeleteConfirm.value = false
  }
}

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
}
function reviewColor(status) {
  return { APPROVED: '#22c55e', DECLINED: '#f87171', REVISION_REQUESTED: '#60a5fa', PENDING: '#f59e0b' }[status] || '#333'
}
</script>
