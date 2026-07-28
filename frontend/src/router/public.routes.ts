import type { RouteRecordRaw } from 'vue-router'

<<<<<<< HEAD
const publicRoutes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: () => import('@/views/Home.vue'), meta: { requiresAuth: false } },
  { path: '/products', name: 'products', component: () => import('@/views/Products.vue'), meta: { requiresAuth: false } },
  { path: '/product-detail/:id', name: 'product-detail', component: () => import('@/views/ProductDetail.vue'), meta: { requiresAuth: false } },
  { path: '/cart', name: 'cart', component: () => import('@/views/Cart.vue'), meta: { requiresAuth: false } },
=======
export const publicRoutes: RouteRecordRaw[] = [
  // Customer & Public routes
  { path: '/', name: 'home', component: () => import('@/views/Home.vue') },
  { path: '/about', name: 'about', component: () => import('@/views/About.vue') },
  { path: '/products', name: 'products', component: () => import('@/views/Products.vue') },
  { path: '/product-detail/:id', name: 'product-detail', component: () => import('@/views/ProductDetail.vue') },
  { path: '/cart', name: 'cart', component: () => import('@/views/Cart.vue') },
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
  { path: '/checkout', name: 'checkout', component: () => import('@/views/Checkout.vue') },
  { path: '/orders', name: 'orders', component: () => import('@/views/Orders.vue') },
  { path: '/order/:id', name: 'order-detail', component: () => import('@/views/OrderDetail.vue') },
  { path: '/profile', name: 'profile', component: () => import('@/views/Profile.vue') },
  { path: '/favorite', name: 'favorite', component: () => import('@/views/Favorite.vue') },
<<<<<<< HEAD
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { guestOnly: true, requiresAuth: false, layout: 'blank' } },
  { path: '/register', name: 'register', component: () => import('@/views/Register.vue'), meta: { guestOnly: true, requiresAuth: false, layout: 'blank' } },
  { path: '/forgot-password', name: 'forgot-password', component: () => import('@/views/ForgotPassword.vue'), meta: { guestOnly: true, requiresAuth: false, layout: 'blank' } },
  { path: '/reset-password', name: 'reset-password', component: () => import('@/views/ResetPassword.vue'), meta: { guestOnly: true, requiresAuth: false, layout: 'blank' } },
  { path: '/oauth-callback', name: 'oauth-callback', component: () => import('@/views/OAuthCallback.vue'), meta: { requiresAuth: false, layout: 'blank' } },
]

export { publicRoutes }
=======
  { path: '/my-rewards', name: 'my-rewards', component: () => import('@/views/MyRewards.vue') },
  { path: '/guide/size', name: 'size-guide', component: () => import('@/views/SizeGuide.vue') },
  { path: '/policy/returns', name: 'returns-policy', component: () => import('@/views/ReturnsPolicy.vue') },
  { path: '/terms-of-service', name: 'terms-of-service', component: () => import('@/views/TermsOfService.vue') },
  { path: '/order-tracking', name: 'order-tracking', component: () => import('@/views/OrderTracking.vue') },
  { path: '/policy/privacy', name: 'privacy-policy', component: () => import('@/views/PrivacyPolicy.vue') },
  { path: '/policy/shipping', name: 'shipping-policy', component: () => import('@/views/ShippingPolicy.vue') },
  { path: '/payment-methods', name: 'payment-methods', component: () => import('@/views/PaymentMethods.vue') },
  
  // Auth routes
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { guestOnly: true, layout: 'blank' } },
  { path: '/register', name: 'register', component: () => import('@/views/Register.vue'), meta: { guestOnly: true, layout: 'blank' } },
  { path: '/forgot-password', name: 'forgot-password', component: () => import('@/views/ForgotPassword.vue'), meta: { guestOnly: true, layout: 'blank' } },
  { path: '/reset-password', name: 'reset-password', component: () => import('@/views/ResetPassword.vue'), meta: { guestOnly: true, layout: 'blank' } },
  { path: '/oauth/callback', name: 'oauth-callback', component: () => import('@/views/OAuthCallback.vue'), meta: { guestOnly: true, layout: 'blank' } },
]
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
