import type { RouteRecordRaw } from 'vue-router'

export const publicRoutes: RouteRecordRaw[] = [
  // Customer & Public routes
  { path: '/', name: 'home', component: () => import('@/views/Home.vue') },
  { path: '/about', name: 'about', component: () => import('@/views/About.vue') },
  { path: '/products', name: 'products', component: () => import('@/views/Products.vue') },
  { path: '/product-detail/:id', name: 'product-detail', component: () => import('@/views/ProductDetail.vue') },
  { path: '/cart', name: 'cart', component: () => import('@/views/Cart.vue') },
  { path: '/checkout', name: 'checkout', component: () => import('@/views/Checkout.vue') },
  { path: '/orders', name: 'orders', component: () => import('@/views/Orders.vue') },
  { path: '/order/:id', name: 'order-detail', component: () => import('@/views/OrderDetail.vue') },
  { path: '/profile', name: 'profile', component: () => import('@/views/Profile.vue') },
  { path: '/favorite', name: 'favorite', component: () => import('@/views/Favorite.vue') },
  { path: '/my-rewards', name: 'my-rewards', component: () => import('@/views/MyRewards.vue') },
  { path: '/guide/size', name: 'size-guide', component: () => import('@/views/SizeGuide.vue') },
  { path: '/policy/returns', name: 'returns-policy', component: () => import('@/views/ReturnsPolicy.vue') },
  { path: '/terms-of-service', name: 'terms-of-service', component: () => import('@/views/TermsOfService.vue') },
  { path: '/order-tracking', name: 'order-tracking', component: () => import('@/views/OrderTracking.vue') },
  { path: '/policy/privacy', name: 'privacy-policy', component: () => import('@/views/PrivacyPolicy.vue') },
  { path: '/policy/shipping', name: 'shipping-policy', component: () => import('@/views/ShippingPolicy.vue') },
  { path: '/payment-methods', name: 'payment-methods', component: () => import('@/views/PaymentMethods.vue') },
  { path: '/help', name: 'help', component: () => import('@/views/Help.vue') },
  
  // Auth routes
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { guestOnly: true, layout: 'blank' } },
  { path: '/register', name: 'register', component: () => import('@/views/Register.vue'), meta: { guestOnly: true, layout: 'blank' } },
  { path: '/forgot-password', name: 'forgot-password', component: () => import('@/views/ForgotPassword.vue'), meta: { guestOnly: true, layout: 'blank' } },
  { path: '/reset-password', name: 'reset-password', component: () => import('@/views/ResetPassword.vue'), meta: { guestOnly: true, layout: 'blank' } },
  { path: '/oauth/callback', name: 'oauth-callback', component: () => import('@/views/OAuthCallback.vue'), meta: { guestOnly: true, layout: 'blank' } },
]
