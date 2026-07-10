import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // Public
    { path: '/',        redirect: '/login' },
    { path: '/login',   component: () => import('@/views/auth/LoginView.vue'),    meta: { guest: true } },
    { path: '/register',component: () => import('@/views/auth/RegisterView.vue'), meta: { guest: true } },

    // Student
    {
      path: '/student',
      component: () => import('@/views/student/StudentLayout.vue'),
      meta: { requiresAuth: true, role: 'STUDENT' },
      children: [
        { path: '',          redirect: '/student/projects' },
        { path: 'projects',  component: () => import('@/views/student/StudentProjectsView.vue') },
        { path: 'projects/submit', component: () => import('@/views/student/SubmitProjectView.vue') },
        { path: 'projects/:id',    component: () => import('@/views/student/StudentProjectDetailView.vue') }
      ]
    },

    // Professor
    {
      path: '/professor',
      component: () => import('@/views/professor/ProfessorLayout.vue'),
      meta: { requiresAuth: true, role: 'PROFESSOR' },
      children: [
        { path: '',          redirect: '/professor/dashboard' },
        { path: 'dashboard', component: () => import('@/views/professor/ProfessorDashboardView.vue') },
        { path: 'projects',  component: () => import('@/views/professor/ProfessorProjectsView.vue') },
        { path: 'projects/:id', component: () => import('@/views/professor/ProfessorProjectDetailView.vue') }
      ]
    },

    // 404
    { path: '/:pathMatch(.*)*', component: () => import('@/views/NotFoundView.vue') }
  ]
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return next('/login')
  }
  if (to.meta.guest && auth.isLoggedIn) {
    return next(auth.isProfessor ? '/professor' : '/student')
  }
  if (to.meta.role && auth.user?.role !== to.meta.role) {
    return next(auth.isProfessor ? '/professor' : '/student')
  }

  next()
})

export default router
