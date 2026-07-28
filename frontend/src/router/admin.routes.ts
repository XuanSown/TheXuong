import type { RouteRecordRaw } from 'vue-router'
import AdminLayoutWrapper from './AdminLayoutWrapper.vue'

const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: AdminLayoutWrapper,
    meta: { requiresAdmin: true, layout: 'admin' },
    children: [
      { path: '', redirect: { name: 'admin-statistics-dashboard' } },
      { path: 'users', name: 'admin-users', component: () => import('@/views/admin/AdminUsers.vue') },
      { path: 'orders', name: 'admin-orders', component: () => import('@/views/admin/AdminOrders.vue') },
      { path: 'products', name: 'admin-products', component: () => import('@/views/admin/AdminProducts.vue') },
      { path: 'products/:id/edit', name: 'admin-products-edit', component: () => import('@/views/admin/AdminProductEdit.vue') },
      { path: 'loyalty/vouchers', name: 'admin-vouchers', component: () => import('@/views/admin/AdminVoucher.vue') },
      { path: 'statistics', name: 'admin-statistics-dashboard', component: () => import('@/views/admin/AdminStatistics.vue') },
      { path: 'loyalty/tiers', name: 'admin-tiers', redirect: { name: 'admin-users' } },
    ],
  },
]

export { adminRoutes }