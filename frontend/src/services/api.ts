import http from './http'
import { authService } from './auth.service'
import { productService } from './product.service'
import { cartService } from './cart.service'
import { orderService } from './order.service'
import { adminService } from './admin.service'
import { checkoutService } from './checkout.service'

export const api = {
  // HTTP configuration overrides and generic methods
  get: http.get.bind(http),
  post: http.post.bind(http),
  put: http.put.bind(http),
  patch: http.patch.bind(http),
  delete: http.delete.bind(http),

  // Auth
  ...authService,
  changePassword: authService.changePassword,

  // Products
  ...productService,

  // Cart
  ...cartService,

  // Orders
  ...orderService,

  // Checkout
  ...checkoutService,

  // Admin APIs
  getAdminUsers: adminService.getUsers,
  toggleUserActive: adminService.toggleUserActive,
  updateUserRole: adminService.updateUserRole,
  createAdminUser: adminService.createUser,
  getAdminProducts: adminService.getProducts,
  getAdminProduct: adminService.getProduct,
  createAdminProduct: adminService.createProduct,
  updateAdminProduct: adminService.updateProduct,
  deleteAdminProduct: adminService.deleteProduct,
  uploadImage: adminService.uploadImage,
  deleteImage: adminService.deleteImage,
  getAdminOrders: adminService.getOrders,
  updateOrderStatus: adminService.updateOrderStatus,
  getStatistics: adminService.getStatistics,
getSizeCatalog: adminService.getSizeCatalog,
getSizeTypes: adminService.getSizeTypes,
createSizeCatalogItem: adminService.createSizeCatalogItem,
toggleSizeCatalogActive: adminService.toggleSizeCatalogActive,
deleteSizeCatalogItem: adminService.deleteSizeCatalogItem
}

export default api
