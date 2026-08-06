## Backend (Java Services & Controllers)

### AddressService.java
- [ ] `listByUser()`
- [ ] `create()`
- [ ] `update()`
- [ ] `delete()`
- [ ] `setDefault()`
- [ ] `getOwnedOrThrow()`

### CartService.java
- [ ] `getCartByUser()`
- [ ] `addToCart()`
- [ ] `removeCartItem()`
- [ ] `updateCartItemQuantity()`
- [ ] `clearCart()`

### ChatbotService.java
- [ ] `getAllProductsForChatbot()`
- [ ] `getAllFaqsForChatbot()`
- [ ] `getChatMemory()`
- [ ] `saveChatMemory()`
- [ ] `trackOrder()`
- [ ] `logInteraction()`

### CloudflareR2Service.java
- [ ] `uploadMultiple()`
- [ ] `deleteFile()`

### EmailService.java
- [ ] `sendEmail()`
- [ ] `sendNewPassword()`
- [ ] `sendPasswordResetLink()`
- [ ] `sendPasswordChangedConfirmation()`
- [ ] `sendVipWelcome()`
- [ ] `sendVipDowngraded()`
- [ ] `sendVipExpiryWarning()`
- [ ] `sendPointsEarned()`
- [ ] `sendVoucherRedeemed()`
- [ ] `sendVoucherExpiring()`

### InventoryService.java
- [ ] `deductStock()`
- [ ] `restoreStock()`

### OrderEventService.java
- [ ] `recordTransition()`

### OrderService.java
- [ ] `placeOrder()`
- [ ] `getOrderByIdAndUser()`
- [ ] `updateOrderInfo()`
- [ ] `cancelOrder()`
- [ ] `confirmReceived()`
- [ ] `refundOrder()`
- [ ] `adminUpdateStatus()`

### PasswordResetService.java
- [ ] `createPasswordResetToken()`
- [ ] `resetPassword()`

### PointService.java
- [ ] `earnPoints()`
- [ ] `spendPoints()`
- [ ] `reversePoints()`
- [ ] `refundSpentPoints()`
- [ ] `adjustPoints()`
- [ ] `expireOldPoints()`
- [ ] `getOrCreateUserPoints()`
- [ ] `getCurrentPoints()`
- [ ] `getHistory()`

### PointTierService.java
- [ ] `getTierForUser()`
- [ ] `getLoyaltyProgress()`
- [ ] `upgradeTierIfEligible()`
- [ ] `setFirstOrderTier()`
- [ ] `updateTierManually()`

### SizeService.java
- [ ] `createVariants()`
- [ ] `updateVariants()`

### TierReevaluateService.java
- [ ] `reevaluateUser()`
- [ ] `reevaluateAllActiveVip()`

### UserService.java
- [ ] `getUserByEmail()`
- [ ] `getUserByEmailWithAddresses()`
- [ ] `getUserById()`
- [ ] `updateProfile()`
- [ ] `updateProfile()`
- [ ] `changePassword()`
- [ ] `toggleActive()`
- [ ] `setRole()`
- [ ] `deleteUser()`
- [ ] `createUser()`

### VNPayService.java
- [ ] `createOrder()`

### VoucherService.java
- [ ] `generateUniqueCode()`
- [ ] `redeemVoucher()`
- [ ] `issueVoucherToUser()`
- [ ] `validateAndGetDiscount()`
- [ ] `markAsUsed()`
- [ ] `restoreVoucher()`
- [ ] `expireOldVouchers()`
- [ ] `getActiveCatalog()`
- [ ] `getUserVouchers()`
- [ ] `getUserVouchersByStatus()`
- [ ] `getVouchers()`
- [ ] `getVoucher()`
- [ ] `createVoucher()`
- [ ] `updateVoucher()`
- [ ] `deleteVoucher()`
- [ ] `bulkAction()`
- [ ] `getStats()`

### VoucherValidator.java
- [ ] `validateCreate()`
- [ ] `validateUpdate()`
- [ ] `generateUniqueCode()`

### AddressRestController.java
- [ ] `list()`
- [ ] `create()`
- [ ] `update()`

### AdminUserRestController.java
- [ ] `getAllUsers()`
- [ ] `toggleActive()`

### CategoryRestController.java
- [ ] `getAllCategories()`
- [ ] `getSports()`
- [ ] `getBrands()`
- [ ] `CategoryDto()`

### VoucherRestController.java
- [ ] `getVouchers()`
- [ ] `getVoucher()`
- [ ] `createVoucher()`
- [ ] `updateVoucher()`
- [ ] `deleteVoucher()`
- [ ] `bulkAction()`
- [ ] `getStats()`

### LoyaltyApiController.java
- [ ] `redeemVoucher()`
- [ ] `getMyVouchers()`

## Frontend (TypeScript Stores & Utils)

### address.store.ts
- [ ] `useAddressStore()`

### auth.store.ts
- [ ] `useAuthStore()`

### cart.store.ts
- [ ] `useCartStore()`

### favorite.store.ts
- [ ] `useFavoriteStore()`

### order.store.ts
- [ ] `useOrderStore()`

### formatters.ts
- [ ] `formatCurrency()`

### validators.ts
- [ ] `emailSchema()`
- [ ] `passwordSchema()`
- [ ] `phoneSchema()`
- [ ] `requiredStringSchema()`
- [ ] `positiveNumberSchema()`
- [ ] `loginSchema()`
- [ ] `registerSchema()`
- [ ] `forgotPasswordSchema()`
- [ ] `resetPasswordSchema()`
- [ ] `productSchema()`
- [ ] `checkoutSchema()`

### vn-regions.ts
- [ ] `getProvinces()`
- [ ] `getDistricts()`
- [ ] `getWards()`
- [ ] `findProvince()`
- [ ] `findDistrict()`
- [ ] `findWard()`
- [ ] `formatAddress()`
- [ ] `matchByGoogleComponents()`
- [ ] `norm()`
- [ ] `findProvinceByName()`
- [ ] `findDistrictByName()`
- [ ] `findWardByName()`
- [ ] `get()`

