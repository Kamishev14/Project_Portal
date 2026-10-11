import { defineStore } from 'pinia'
import { ref } from 'vue'
import { projectApi, settingsApi } from '@/api'

export const useProjectStore = defineStore('projects', () => {
  const myProjects     = ref([])
  const allProjects    = ref([])
  const currentProject = ref(null)
  const stats          = ref(null)
  const professorOptions = ref([])
  const submissionDeadline = ref(null)
  const pagination     = ref({ page: 0, size: 10, totalPages: 0, totalElements: 0 })
  const loading        = ref(false)
  const professorOptionsLoading = ref(false)
  const error          = ref(null)

  function parseApiError(e, fallback) {
    const data = e.response?.data

    if (typeof data?.message === 'string' && data.message.trim()) {
      return data.message
    }

    if (typeof data === 'string' && data.trim()) {
      return data
    }

    if (data && typeof data === 'object') {
      const firstMessage = Object.values(data).find(value => typeof value === 'string' && value.trim())
      if (firstMessage) return firstMessage
    }

    return fallback
  }

  // ── Student ────────────────────────────────────────────────────────────────

  async function submitProject(data) {
    loading.value = true
    error.value   = null
    try {
      const res = await projectApi.submit(data)
      myProjects.value.unshift(res.data)
      return res.data
    } catch (e) {
      error.value = parseApiError(e, 'Submission failed')
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchMyProjects() {
    loading.value = true
    try {
      const res = await projectApi.getMyProjects()
      myProjects.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchMyProject(id) {
    loading.value = true
    try {
      const res = await projectApi.getMyProject(id)
      currentProject.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function editProject(id, data) {
    loading.value = true
    error.value   = null
    try {
      const res = await projectApi.editProject(id, data)
      currentProject.value = res.data
      // Update in myProjects list if present
      const idx = myProjects.value.findIndex(p => p.id === id)
      if (idx !== -1) myProjects.value[idx] = res.data
      return res.data
    } catch (e) {
      error.value = parseApiError(e, 'Edit failed')
      throw e
    } finally {
      loading.value = false
    }
  }

  async function deleteProject(id) {
    loading.value = true
    error.value   = null
    try {
      await projectApi.deleteProject(id)
      myProjects.value = myProjects.value.filter(p => p.id !== id)
      currentProject.value = null
    } catch (e) {
      error.value = parseApiError(e, 'Delete failed')
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchProfessorOptions(force = false) {
    if (!force && professorOptions.value.length > 0) return professorOptions.value

    professorOptionsLoading.value = true
    error.value = null
    try {
      const res = await projectApi.getProfessorOptions()
      professorOptions.value = res.data
      return res.data
    } catch (e) {
      error.value = parseApiError(e, 'Failed to load professors')
      throw e
    } finally {
      professorOptionsLoading.value = false
    }
  }

  // ── Professor ──────────────────────────────────────────────────────────────

  async function fetchAllProjects(params = {}) {
    loading.value = true
    try {
      const res = await projectApi.getAll(params)
      allProjects.value = res.data.content
      pagination.value  = {
        page:          res.data.number,
        size:          res.data.size,
        totalPages:    res.data.totalPages,
        totalElements: res.data.totalElements
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchProjectById(id) {
    loading.value = true
    try {
      const res = await projectApi.getById(id)
      currentProject.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function reviewProject(id, data) {
    loading.value = true
    error.value   = null
    try {
      const res = await projectApi.review(id, data)
      currentProject.value = res.data
      const idx = allProjects.value.findIndex(p => p.id === id)
      if (idx !== -1) allProjects.value[idx] = res.data
      return res.data
    } catch (e) {
      error.value = parseApiError(e, 'Review failed')
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchStats() {
    const res = await projectApi.getStats()
    stats.value = res.data
  }

  async function fetchSubmissionDeadline() {
    try {
      const res = await settingsApi.getSubmissionDeadline()
      submissionDeadline.value = res.data.deadline ? new Date(res.data.deadline) : null
    } catch (e) {
      // Gracefully handle error
      submissionDeadline.value = null
    }
  }

  async function updateSubmissionDeadline(deadline) {
    loading.value = true
    error.value = null
    try {
      const res = await settingsApi.updateSubmissionDeadline({ deadline })
      submissionDeadline.value = res.data.deadline ? new Date(res.data.deadline) : null
      return res.data
    } catch (e) {
      error.value = parseApiError(e, 'Failed to update deadline')
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    myProjects, allProjects, currentProject, stats, professorOptions, submissionDeadline, pagination,
    loading, professorOptionsLoading, error,
    submitProject, fetchMyProjects, fetchMyProject, editProject, deleteProject, fetchProfessorOptions,
    fetchAllProjects, fetchProjectById, reviewProject, fetchStats, fetchSubmissionDeadline, updateSubmissionDeadline
  }
})
