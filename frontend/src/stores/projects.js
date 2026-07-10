import { defineStore } from 'pinia'
import { ref } from 'vue'
import { projectApi } from '@/api'

export const useProjectStore = defineStore('projects', () => {
  const myProjects     = ref([])
  const allProjects    = ref([])
  const currentProject = ref(null)
  const stats          = ref(null)
  const pagination     = ref({ page: 0, size: 10, totalPages: 0, totalElements: 0 })
  const loading        = ref(false)
  const error          = ref(null)

  // ── Student ────────────────────────────────────────────────────────────────

  async function submitProject(data) {
    loading.value = true
    error.value   = null
    try {
      const res = await projectApi.submit(data)
      myProjects.value.unshift(res.data)
      return res.data
    } catch (e) {
      error.value = e.response?.data?.message || 'Submission failed'
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
      error.value = e.response?.data?.message || 'Edit failed'
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
      error.value = e.response?.data?.message || 'Delete failed'
      throw e
    } finally {
      loading.value = false
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
      error.value = e.response?.data?.message || 'Review failed'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchStats() {
    const res = await projectApi.getStats()
    stats.value = res.data
  }

  return {
    myProjects, allProjects, currentProject, stats, pagination,
    loading, error,
    submitProject, fetchMyProjects, fetchMyProject, editProject, deleteProject,
    fetchAllProjects, fetchProjectById, reviewProject, fetchStats
  }
})
