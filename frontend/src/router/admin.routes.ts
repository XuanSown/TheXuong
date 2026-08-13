import type { RouteRecordRaw } from 'vue-router'

export const adminRoutes: RouteRecordRaw[] = [
  { path: '/admin', redirect: '/admin/users' },
  { path: '/admin/products', name: 'admin-products', component: () => import('@/router/AdminLayoutWrapper.vue'), children: [
    { path: '', name: 'admin-products-list', component: () => import('@/views/admin/AdminProducts.vue') }
  ], meta: { requiresAdmin: true, layout: 'admin' } },
  { path: '/admin/products/create', name: 'admin-product-create', component: () => import('@/router/AdminLayoutWrapper.vue'), children: [
    { path: '', name: 'admin-product-create-inner', component: () => import('@/views/admin/AdminProductEdit.vue') }
  ], meta: { requiresAdmin: true, layout: 'admin' } },
  { path: '/admin/products/:id/edit', name: 'admin-product-edit', component: () => import('@/router/AdminLayoutWrapper.vue'), children: [
    { path: '', name: 'admin-product-edit-inner', component: () => import('@/views/admin/AdminProductEdit.vue') }
  ], meta: { requiresAdmin: true, layout: 'admin' } },
  { path: '/admin/orders', name: 'admin-orders', component: () => import('@/router/AdminLayoutWrapper.vue'), children: [
    { path: '', name: 'admin-orders-list', component: () => import('@/views/admin/AdminOrders.vue') }
  ], meta: { requiresAdmin: true, layout: 'admin' } },
  { path: '/admin/users', name: 'admin-users', component: () => import('@/router/AdminLayoutWrapper.vue'), children: [
    { path: '', name: 'admin-users-list', component: () => import('@/views/admin/AdminUsers.vue') }
  ], meta: { requiresAdmin: true, layout: 'admin' } },
  { path: '/admin/statistics', name: 'admin-statistics', component: () => import('@/router/AdminLayoutWrapper.vue'), children: [
    { path: '', name: 'admin-statistics-dashboard', component: () => import('@/views/admin/AdminStatistics.vue') }
  ], meta: { requiresAdmin: true, layout: 'admin' } },
  { path: '/admin/loyalty/vouchers', name: 'admin-vouchers', component: () => import('@/router/AdminLayoutWrapper.vue'), children: [
    { path: '', name: 'admin-vouchers-list', component: () => import('@/views/admin/AdminVoucher.vue') }
  ], meta: { requiresAdmin: true, layout: 'admin' } },
  { path: '/admin/loyalty/tiers', name: 'admin-tiers', component: () => import('@/router/AdminLayoutWrapper.vue'), children: [
    { path: '', name: 'admin-tiers-list', component: () => import('@/views/admin/AdminTiers.vue') }
  ], meta: { requiresAdmin: true, layout: 'admin' } },
  { path: '/admin/audit-logs', name: 'admin-audit-logs', component: () => import('@/router/AdminLayoutWrapper.vue'), children: [
    { path: '', name: 'admin-audit-logs-list', component: () => import('@/views/admin/AdminAuditLogs.vue') }
  ], meta: { requiresAdmin: true, layout: 'admin' } }
]
