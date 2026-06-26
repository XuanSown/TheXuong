import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/products',
    name: 'products',
    component: () => import('@/views/Products.vue')
  },
  {
    path: '/product-detail/:id',
    name: 'product-detail',
    component: () => import('@/views/ProductDetail.vue')
  },
  {
    path: '/cart',
    name: 'cart',
    component: () => import('@/views/Cart.vue')
  },
  {
    path: '/checkout',
    name: 'checkout',
    component: () => import('@/views/Checkout.vue')
  },
  {
    path: '/orders',
    name: 'orders',
    component: () => import('@/views/Orders.vue')
  },
  {
    path: '/order/:id',
    name: 'order-detail',
    component: () => import('@/views/OrderDetail.vue')
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/Profile.vue')
  },
  {
    path: '/favorite',
    name: 'favorite',
    component: () => import('@/views/Favorite.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/Register.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: () => import('@/views/ForgotPassword.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/oauth/callback',
    name: 'oauth-callback',
    component: () => import('@/views/OAuthCallback.vue')
  },
  {
    path: '/admin/products',
    name: 'admin-products',
    component: () => import('@/router/AdminLayoutWrapper.vue'),
    children: [
      {
        path: '',
        name: 'admin-products-list',
        component: () => import('@/views/admin/AdminProducts.vue')
      }
    ],
    meta: { requiresAdmin: true, layout: 'admin' }
  },
  {
    path: '/admin/products/create',
    name: 'admin-product-create',
    component: () => import('@/router/AdminLayoutWrapper.vue'),
    children: [
      {
        path: '',
        name: 'admin-product-create-inner',
        component: () => import('@/views/admin/AdminProductEdit.vue')
      }
    ],
    meta: { requiresAdmin: true, layout: 'admin' }
  },
  {
    path: '/admin/products/:id/edit',
    name: 'admin-product-edit',
    component: () => import('@/router/AdminLayoutWrapper.vue'),
    children: [
      {
        path: '',
        name: 'admin-product-edit-inner',
        component: () => import('@/views/admin/AdminProductEdit.vue')
      }
    ],
    meta: { requiresAdmin: true, layout: 'admin' }
  },
  {
    path: '/admin/orders',
    name: 'admin-orders',
    component: () => import('@/router/AdminLayoutWrapper.vue'),
    children: [
      {
        path: '',
        name: 'admin-orders-list',
        component: () => import('@/views/admin/AdminOrders.vue')
      }
    ],
    meta: { requiresAdmin: true, layout: 'admin' }
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: () => import('@/router/AdminLayoutWrapper.vue'),
    children: [
      {
        path: '',
        name: 'admin-users-list',
        component: () => import('@/views/admin/AdminUsers.vue')
      }
    ],
    meta: { requiresAdmin: true, layout: 'admin' }
  },
  {
    path: '/admin/statistics',
    name: 'admin-statistics',
    component: () => import('@/router/AdminLayoutWrapper.vue'),
    children: [
      {
        path: '',
        name: 'admin-statistics-dashboard',
        component: () => import('@/views/admin/AdminStatistics.vue')
      }
    ],
    meta: { requiresAdmin: true, layout: 'admin' }
  },
  {
    path: '/admin/loyalty/vouchers',
    name: 'admin-vouchers',
    component: () => import('@/router/AdminLayoutWrapper.vue'),
    children: [
      {
        path: '',
        name: 'admin-vouchers-list',
        component: () => import('@/views/admin/AdminVoucher.vue')
      }
    ],
    meta: { requiresAdmin: true, layout: 'admin' }
  },
  // Catch all - 404
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // If route requires admin
  if (to.meta.requiresAdmin) {
    // Check if user is logged in and has admin role
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

  // If route requires auth (except guestOnly routes)
  if (to.meta.requiresAuth !== false && !to.meta.guestOnly) {
    // Public routes that don't require auth
    const publicRoutes = ['login', 'register', 'forgot-password', 'home', 'products', 'product-detail', 'cart']
    if (!publicRoutes.includes(to.name as string) && !authStore.isAuthenticated) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }
  }

  // Guest-only routes (login, register) should redirect if already logged in
  if (to.meta.guestOnly && authStore.isAuthenticated) {
    next({ name: 'home' })
    return
  }

  next()
})

export default router
