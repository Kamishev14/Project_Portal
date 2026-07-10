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
import { ref } from 'vue'
import { useProjectStore } from '@/stores/projects'
import QrCodeCard from '@/components/common/QrCodeCard.vue'

const store            = useProjectStore()
const form             = ref({ title: '', description: '' })
const submitted        = ref(false)
const submittedProject = ref(null)
const errorMsg         = ref('')

async function handleSubmit() {
  errorMsg.value = ''
  try {
    submittedProject.value = await store.submitProject(form.value)
    submitted.value = true
  } catch (e) {
    errorMsg.value = store.error || 'Submission failed. Please try again.'
  }
}
</script>
