import type { RouteRecordRaw } from 'vue-router'

const publicRoutes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: () => import('@/views/Home.vue'), meta: { requiresAuth: false } },
  { path: '/products', name: 'products', component: () => import('@/views/Products.vue'), meta: { requiresAuth: false } },
  { path: '/product-detail/:id', name: 'product-detail', component: () => import('@/views/ProductDetail.vue'), meta: { requiresAuth: false } },
  { path: '/cart', name: 'cart', component: () => import('@/views/Cart.vue'), meta: { requiresAuth: false } },
  { path: '/checkout', name: 'checkout', component: () => import('@/views/Checkout.vue') },
  { path: '/orders', name: 'orders', component: () => import('@/views/Orders.vue') },
  { path: '/order/:id', name: 'order-detail', component: () => import('@/views/OrderDetail.vue') },
  { path: '/profile', name: 'profile', component: () => import('@/views/Profile.vue') },
  { path: '/favorite', name: 'favorite', component: () => import('@/views/Favorite.vue') },
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { guestOnly: true, requiresAuth: false, layout: 'blank' } },
  { path: '/register', name: 'register', component: () => import('@/views/Register.vue'), meta: { guestOnly: true, requiresAuth: false, layout: 'blank' } },
  { path: '/forgot-password', name: 'forgot-password', component: () => import('@/views/ForgotPassword.vue'), meta: { guestOnly: true, requiresAuth: false, layout: 'blank' } },
  { path: '/reset-password', name: 'reset-password', component: () => import('@/views/ResetPassword.vue'), meta: { guestOnly: true, requiresAuth: false, layout: 'blank' } },
  { path: '/oauth-callback', name: 'oauth-callback', component: () => import('@/views/OAuthCallback.vue'), meta: { requiresAuth: false, layout: 'blank' } },
]

export { publicRoutes }