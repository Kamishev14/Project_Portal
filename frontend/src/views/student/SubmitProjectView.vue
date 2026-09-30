<template>
  <div class="max-w-2xl">
    <RouterLink to="/student/projects" class="text-xs hover:underline" style="color:#555">← Back</RouterLink>

    <!-- Success -->
    <div v-if="submitted" class="mt-6 card text-center space-y-4">
      <p class="text-4xl">🎉</p>
      <h2 class="text-xl font-bold text-white">Submitted!</h2>
      <p class="text-sm" style="color:#666">Your project is in the review queue.</p>
      <QrCodeCard v-if="submittedProject" :project="submittedProject" />
      <div class="flex gap-3 justify-center pt-2">
        <RouterLink to="/student/projects" class="btn-secondary">All Projects</RouterLink>
        <RouterLink :to="`/student/projects/${submittedProject?.id}`" class="btn-primary">View Project</RouterLink>
      </div>
    </div>

    <!-- Form -->
    <template v-else>
      <h1 class="text-2xl font-bold tracking-tight text-white mt-4">Submit a Project Idea</h1>
      <p class="text-sm mt-1 mb-8" style="color:#555">Describe your idea clearly — your professor will review it.</p>

      <form @submit.prevent="handleSubmit" class="space-y-5">
        <div>
          <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Project Title</label>
          <input
            v-model="form.title"
            type="text"
            class="input"
            placeholder="e.g. Smart Campus Navigation App"
            required minlength="5" maxlength="200"
          />
        </div>

        <div>
          <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Description</label>
          <textarea
            v-model="form.description"
            class="input resize-none"
            rows="6"
            placeholder="What problem does it solve? What technologies will you use?"
            required minlength="20"
          ></textarea>
          <p class="text-xs mt-1.5" style="color:#444">
            {{ form.description.length }} chars
            <span v-if="form.description.length < 20" style="color:#f59e0b"> — need {{ 20 - form.description.length }} more</span>
          </p>
        </div>

        <div>
          <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Preferred Professor</label>
          <select v-model="form.preferredProfessorId" class="input">
            <option value="">No preference</option>
            <option
              v-for="professor in store.professorOptions"
              :key="professor.id"
              :value="professor.id"
            >
              {{ professor.name }} — {{ professor.email }}
            </option>
          </select>
          <p class="text-xs mt-1.5" style="color:#444">
            Optional. Students can still submit without choosing a professor.
          </p>
          <p v-if="store.professorOptionsLoading" class="text-xs mt-1.5" style="color:#555">Loading professors...</p>
        </div>

        <div>
          <label class="block text-xs font-medium mb-1.5 tracking-wide uppercase" style="color:#777">Participating Colleagues</label>
          <textarea
            v-model="form.participantStudentIndexes"
            class="input resize-none font-mono"
            rows="3"
            placeholder="Enter 6-digit student indexes, separated by commas or new lines&#10;e.g. 201001, 201045"
          ></textarea>
          <p class="text-xs mt-1.5" style="color:#444">
            Optional. Add classmates who are working on this project with you. They must already be registered student accounts.
          </p>
          <p v-if="parsedParticipantIndexes.length" class="text-xs mt-1.5" style="color:#22c55e">
            {{ parsedParticipantIndexes.length }} participant{{ parsedParticipantIndexes.length !== 1 ? 's' : '' }} detected.
          </p>
        </div>

        <div v-if="errorMsg" class="px-3 py-2.5 rounded-md text-sm" style="background:#1a0000;color:#f87171;border:1px solid #3a0000">
          {{ errorMsg }}
        </div>

        <div class="flex gap-3">
          <RouterLink to="/student/projects" class="btn-secondary">Cancel</RouterLink>
          <button type="submit" class="btn-primary" :disabled="store.loading">
            <span v-if="store.loading" class="mr-2 opacity-70">↻</span>
            Submit Idea
          </button>
        </div>
      </form>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useProjectStore } from '@/stores/projects'
import QrCodeCard from '@/components/common/QrCodeCard.vue'

const store            = useProjectStore()
const form             = ref({ title: '', description: '', preferredProfessorId: '', participantStudentIndexes: '' })
const submitted        = ref(false)
const submittedProject = ref(null)
const errorMsg         = ref('')

const parsedParticipantIndexes = computed(() => parseParticipantIndexes(form.value.participantStudentIndexes))

onMounted(async () => {
  try {
    await store.fetchProfessorOptions()
  } catch (e) {
    errorMsg.value = store.error || 'Failed to load professors'
  }
})

async function handleSubmit() {
  errorMsg.value = ''

  const invalidParticipant = parsedParticipantIndexes.value.find(index => !/^\d{6}$/.test(index))
  if (invalidParticipant) {
    errorMsg.value = `Participant index must be exactly 6 digits: ${invalidParticipant}`
    return
  }

  try {
    submittedProject.value = await store.submitProject({
      title: form.value.title,
      description: form.value.description,
      preferredProfessorId: form.value.preferredProfessorId || null,
      participantStudentIndexes: parsedParticipantIndexes.value
    })
    submitted.value = true
  } catch (e) {
    errorMsg.value = store.error || 'Submission failed. Please try again.'
  }
}

function parseParticipantIndexes(rawValue) {
  return rawValue
    .split(/[\n,]/)
    .map(value => value.trim())
    .filter(Boolean)
    .filter((value, index, list) => list.indexOf(value) === index)
}
</script>
