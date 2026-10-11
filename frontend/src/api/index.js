import api from './axios'

export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login:    (data) => api.post('/auth/login', data)
}

export const projectApi = {
  // Student
  submit:            (data)       => api.post('/projects', data),
  getMyProjects:     ()           => api.get('/projects/mine'),
  getMyProject:      (id)         => api.get(`/projects/mine/${id}`),
  editProject:       (id, data)   => api.put(`/projects/mine/${id}`, data),
  deleteProject:     (id)         => api.delete(`/projects/mine/${id}`),
  getProfessorOptions: ()         => api.get('/projects/professors/options'),

  // Professor
  getAll:            (params)     => api.get('/projects', { params }),
  getById:           (id)         => api.get(`/projects/${id}`),
  getByStudentIndex: (index)      => api.get(`/projects/student/${index}`),
  review:            (id, data)   => api.patch(`/projects/${id}/review`, data),
  getStats:          ()           => api.get('/projects/stats'),

  // Shared
  getQrUrl:          (id)         => `/api/projects/${id}/qr`
}

export const settingsApi = {
  getSubmissionDeadline: ()       => api.get('/settings/submission-deadline'),
  updateSubmissionDeadline: (data) => api.put('/settings/submission-deadline', data)
}

