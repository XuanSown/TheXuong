import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

import { adminRoutes } from './admin.routes'
import { publicRoutes } from './public.routes'

const routes: RouteRecordRaw[] = [
  ...publicRoutes,
  ...adminRoutes,
  // Catch all - 404
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  // Initialize auth state before resolving any route
  if (!authStore.isInitialized) {
    try {
      await authStore.fetchUser()
    } catch {
      // Continue, user is just not logged in
    }
  }

  // If route requires admin
  if (to.meta.requiresAdmin) {
    if (!authStore.isAuthenticated) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }
    if (!authStore.isAdmin) {
      next({ name: 'home' })
      return
    }
    next()
    return
  }

  // If user is pure admin, redirect customer routes to admin panel
  if (authStore.isAuthenticated && authStore.isAdmin && !authStore.isCustomer) {
    const customerRoutes = ['home', 'products', 'product-detail', 'cart', 'checkout', 'orders', 'order-detail', 'profile', 'favorite']
    if (customerRoutes.includes(to.name as string) && !to.meta.requiresAdmin) {
      next({ name: 'admin-statistics-dashboard' })
      return
    }
  }

  // If route requires auth (except guestOnly routes)
  if (to.meta.requiresAuth !== false && !to.meta.guestOnly) {
    const publicRoutes = ['login', 'register', 'forgot-password', 'reset-password', 'home', 'products', 'product-detail', 'cart']
    if (!publicRoutes.includes(to.name as string) && !authStore.isAuthenticated) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }
  }

  // Guest-only routes (login, register) should redirect if already logged in
  if (to.meta.guestOnly && authStore.isAuthenticated) {
    if (authStore.isAdmin) {
      next({ name: 'admin-statistics-dashboard' })
    } else {
      next({ name: 'home' })
    }
    return
  }

  next()
})

export default router
