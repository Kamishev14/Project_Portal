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
              <div>
                <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Preferred Professor</label>
                <select v-model="editForm.preferredProfessorId" class="input">
                  <option value="">No preference</option>
                  <option
                    v-for="professor in store.professorOptions"
                    :key="professor.id"
                    :value="professor.id"
                  >
                    {{ professor.name }} — {{ professor.email }}
                  </option>
                </select>
                <p v-if="store.professorOptionsLoading" class="text-xs mt-1.5" style="color:#555">Loading professors...</p>
              </div>
              <div>
                <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Participating Colleagues</label>
                <textarea
                  v-model="editForm.participantStudentIndexes"
                  class="input resize-none font-mono"
                  rows="3"
                  placeholder="Enter 6-digit student indexes separated by commas or new lines"
                ></textarea>
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

          <div class="card">
            <p class="text-xs font-medium tracking-widest uppercase mb-3" style="color:#555">Team & Professor</p>

            <div class="space-y-4">
              <div>
                <p class="text-xs uppercase tracking-wide mb-1" style="color:#444">Preferred Professor</p>
                <p v-if="project.preferredProfessor" class="text-sm" style="color:#ccc">
                  {{ project.preferredProfessor.name }} · {{ project.preferredProfessor.email }}
                </p>
                <p v-else class="text-sm italic" style="color:#555">No preferred professor selected.</p>
              </div>

              <div>
                <p class="text-xs uppercase tracking-wide mb-1" style="color:#444">Participating Colleagues</p>
                <div v-if="project.participants?.length" class="space-y-2">
                  <div
                    v-for="participant in project.participants"
                    :key="participant.id"
                    class="rounded-md px-3 py-2"
                    style="background:#111;border:1px solid #222"
                  >
                    <p class="text-sm text-white">{{ participant.name }}</p>
                    <p class="text-xs font-mono" style="color:#22c55e">{{ participant.studentIndex }}</p>
                  </div>
                </div>
                <p v-else class="text-sm italic" style="color:#555">No colleagues added yet.</p>
              </div>
            </div>
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
import { useToastStore } from '@/stores/toast'
import StatusBadge from '@/components/common/StatusBadge.vue'
import BaseSpinner from '@/components/common/BaseSpinner.vue'
import QrCodeCard from '@/components/common/QrCodeCard.vue'

const route = useRoute()
const router = useRouter()
const store = useProjectStore()
const toast = useToastStore()
const project = computed(() => store.currentProject)

const editing = ref(false)
const editForm = ref({ title: '', description: '', preferredProfessorId: '', participantStudentIndexes: '' })

const showDeleteConfirm = ref(false)

const canEdit = computed(() =>
  project.value &&
  ['PENDING', 'REVISION_REQUESTED'].includes(project.value.status)
)

onMounted(async () => {
  await store.fetchMyProject(route.params.id)
  try {
    await store.fetchProfessorOptions()
  } catch (e) {
    // keep the detail view usable even if professor options fail to load
  }
})

function startEdit() {
  editForm.value = {
    title: project.value.title,
    description: project.value.description,
    preferredProfessorId: project.value.preferredProfessor?.id || '',
    participantStudentIndexes: participantIndexesAsText(project.value.participants)
  }
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function handleEdit() {
  const parsedParticipantIndexes = parseParticipantIndexes(editForm.value.participantStudentIndexes)
  const invalidParticipant = parsedParticipantIndexes.find(index => !/^\d{6}$/.test(index))
  if (invalidParticipant) {
    toast.error(`Participant index must be exactly 6 digits: ${invalidParticipant}`)
    return
  }

  try {
    await store.editProject(route.params.id, {
      title: editForm.value.title,
      description: editForm.value.description,
      preferredProfessorId: editForm.value.preferredProfessorId || null,
      participantStudentIndexes: parsedParticipantIndexes
    })
    editing.value = false
    toast.success('Project updated successfully')
  } catch (e) {
    toast.error(store.error || 'Failed to save changes')
  }
}

function confirmDelete() {
  showDeleteConfirm.value = true
}

async function handleDelete() {
  try {
    await store.deleteProject(route.params.id)
    toast.success('Project deleted')
    router.push('/student/projects')
  } catch (e) {
    showDeleteConfirm.value = false
    toast.error('Failed to delete project')
  }
}

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
}

function participantIndexesAsText(participants = []) {
  return participants.map(participant => participant.studentIndex).filter(Boolean).join(', ')
}

function parseParticipantIndexes(rawValue) {
  return rawValue
    .split(/[\n,]/)
    .map(value => value.trim())
    .filter(Boolean)
    .filter((value, index, list) => list.indexOf(value) === index)
}

function reviewColor(status) {
  return { APPROVED: '#22c55e', DECLINED: '#f87171', REVISION_REQUESTED: '#60a5fa', PENDING: '#f59e0b' }[status] || '#333'
}
</script>
