<template>
  <div>
    <RouterLink to="/professor/projects" class="text-xs hover:underline" style="color:#555">← Back</RouterLink>

    <BaseSpinner v-if="store.loading && !project" />

    <template v-else-if="project">
      <div class="mt-5 mb-8">
        <div class="flex items-center gap-2 mb-2">
          <span class="text-xs font-mono" style="color:#444">Queue #{{ project.queueNumber }}</span>
          <StatusBadge :status="project.status" />
        </div>
        <h1 class="text-2xl font-bold tracking-tight text-white">{{ project.title }}</h1>
        <div class="flex items-center gap-2 mt-2">
          <span class="text-xs font-mono font-bold" style="color:#22c55e">{{ project.student.studentIndex }}</span>
          <span class="text-sm" style="color:#666">{{ project.student.name }} · {{ project.student.email }}</span>
          <span class="text-xs" style="color:#444">· {{ formatDate(project.createdAt) }}</span>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
        <!-- Left -->
        <div class="md:col-span-2 space-y-4">
          <div class="card">
            <p class="text-xs font-medium tracking-widest uppercase mb-3" style="color:#555">Description</p>
            <p class="text-sm leading-relaxed whitespace-pre-wrap" style="color:#ccc">{{ project.description }}</p>
          </div>

          <!-- Review form -->
          <div class="card">
            <p class="text-xs font-medium tracking-widest uppercase mb-4" style="color:#555">Submit Review</p>

            <div v-if="reviewSuccess"
                 class="flex items-center gap-2 px-4 py-3 rounded-md text-sm mb-4"
                 style="background:#052e16;color:#22c55e;border:1px solid #14532d">
              ✅ Review submitted — student
              <span class="font-mono font-bold">{{ project.student.studentIndex }}</span>
              notified.
            </div>

            <form @submit.prevent="handleReview" class="space-y-4">
              <div>
                <p class="text-xs font-medium mb-2" style="color:#666">Decision</p>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="opt in decisionOptions"
                    :key="opt.value"
                    type="button"
                    @click="form.status = opt.value"
                    class="decision-btn"
                    :style="form.status === opt.value ? opt.activeStyle : ''"
                  >{{ opt.label }}</button>
                </div>
              </div>

              <div>
                <label class="block text-xs font-medium mb-1.5" style="color:#666">
                  Comment <span style="color:#444">(optional)</span>
                </label>
                <textarea
                  v-model="form.comment"
                  class="input resize-none"
                  rows="4"
                  placeholder="Provide feedback to the student..."
                ></textarea>
              </div>

              <div v-if="errorMsg" class="px-3 py-2.5 rounded-md text-sm" style="background:#1a0000;color:#f87171;border:1px solid #3a0000">
                {{ errorMsg }}
              </div>

              <button type="submit" class="btn-primary" :disabled="!form.status || store.loading">
                <span v-if="store.loading" class="mr-2 opacity-70">↻</span>
                Submit Review
              </button>
            </form>
          </div>

          <!-- History -->
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
                  <span class="text-xs" style="color:#444">{{ formatDate(review.reviewedAt) }}</span>
                </div>
                <p v-if="review.comment" class="text-sm" style="color:#aaa">{{ review.comment }}</p>
                <p v-else class="text-xs italic" style="color:#444">No comment.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Right -->
        <div class="space-y-4">
          <QrCodeCard :project="project" />
          <div class="card text-center">
            <p class="text-xs mb-2" style="color:#555">Other projects by this student</p>
            <button
              @click="searchStudent"
              class="text-sm font-mono font-bold hover:underline"
              style="color:#22c55e"
            >{{ project.student.studentIndex }}</button>
          </div>
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

const route         = useRoute()
const router        = useRouter()
const store         = useProjectStore()
const project       = computed(() => store.currentProject)
const reviewSuccess = ref(false)
const errorMsg      = ref('')
const form          = ref({ status: '', comment: '' })

const decisionOptions = [
  { value: 'APPROVED',           label: '✅ Approve',         activeStyle: 'background:#052e16;color:#22c55e;border-color:#14532d' },
  { value: 'REVISION_REQUESTED', label: '🔄 Request Revision', activeStyle: 'background:#0c1a3a;color:#60a5fa;border-color:#1e3a5f' },
  { value: 'DECLINED',           label: '❌ Decline',          activeStyle: 'background:#2d0000;color:#f87171;border-color:#5a0000' }
]

onMounted(() => store.fetchProjectById(route.params.id))

async function handleReview() {
  errorMsg.value = ''; reviewSuccess.value = false
  try {
    await store.reviewProject(route.params.id, form.value)
    reviewSuccess.value = true
    form.value = { status: '', comment: '' }
  } catch (e) {
    errorMsg.value = store.error || 'Failed to submit review'
  }
}

function searchStudent() {
  router.push({ path: '/professor/projects', query: { searchIndex: project.value.student.studentIndex } })
}

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
}
function reviewColor(status) {
  return { APPROVED: '#22c55e', DECLINED: '#f87171', REVISION_REQUESTED: '#60a5fa' }[status] || '#333'
}
</script>

<style scoped>
.decision-btn {
  padding: 8px 14px; font-size: 0.8125rem; font-weight: 500;
  border-radius: 6px; border: 1px solid #2a2a2a;
  background: #141414; color: #666; cursor: pointer;
  transition: all 0.1s;
}
.decision-btn:hover { border-color: #3a3a3a; color: #ccc; }
</style>
