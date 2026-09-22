import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import AdminDashboardView from '@/views/AdminDashboardView.vue'
import UserDashboardView from '@/views/UserDashboardView.vue'
import ApartmentDetailsView from '@/views/ApartmentDetailsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: AdminDashboardView,
      meta: { role: 'ADMIN' }
    },
    {
      path: '/dashboard',
      name: 'user-dashboard',
      component: UserDashboardView,
      meta: { role: 'USER' }
    },
    {
      path: '/apartments/:id',
      name: 'apartment-details',
      component: ApartmentDetailsView
    },
    {
      path: '/profile/:userId',
      name: 'business-profile',
      component: () => import('@/views/BusinessProfileView.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ]
})

import { auth } from '@/stores/auth'


router.beforeEach(async (to, from, next) => {
  const publicPages = ['/login', '/register']
  const authRequired = !publicPages.includes(to.path)
  if (auth.isAuthenticated === false) {
    await auth.checkAuth()
  }
  if (publicPages.includes(to.path) && auth.isAuthenticated) {
    if (auth.role === 'ADMIN') {
      return next('/admin/dashboard')
    } else {
      return next('/dashboard')
    }
  }
  if (authRequired && !auth.isAuthenticated) {
    return next('/login')
  }
  if (to.meta.role) {
    if (to.meta.role === 'ADMIN' && auth.role !== 'ADMIN') {
      return next('/dashboard')
    }
  }

  next()
})

export default router
