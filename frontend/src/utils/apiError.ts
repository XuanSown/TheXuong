import i18n from '@/i18n'

// Backend trả lỗi dạng { success: false, message: "..." } (một số endpoint dùng key "error").
// API không trả error code, nên map các message VI ổn định sang key i18n.
// Message động (có biến nội suy, VD: "Số dư không đủ. Bạn có X điểm") sẽ rơi vào fallback.
const BACKEND_MESSAGE_KEYS: Record<string, string> = {
  'Email hoặc mật khẩu không đúng': 'backendError.loginFailed',
  'Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.': 'auth.accountLocked',
  'Email đã được đăng ký': 'backendError.emailExists',
  'Xác nhận mật khẩu không khớp': 'backendError.passwordConfirmMismatch',
  'Mat khau hien tai khong dung.': 'backendError.currentPasswordWrong',
  'Mat khau moi phai co it nhat 8 ky tu.': 'backendError.newPasswordTooShort',
  'Tai khoan nay khong co mat khau de xac thuc.': 'backendError.noPasswordAuth',
  'Voucher này hiện không khả dụng.': 'backendError.voucherUnavailable',
  'Voucher này chỉ dành cho khách hàng VIP.': 'backendError.voucherVipOnly',
  'Voucher này không thuộc tài khoản của anh/chị.': 'backendError.voucherNotOwned',
  'Voucher này đã được sử dụng.': 'backendError.voucherUsed',
  'Voucher này đã hết hạn.': 'backendError.voucherExpired',
  'Voucher này đã quá hạn sử dụng.': 'backendError.voucherExpired',
  'Voucher này không hợp lệ hoặc đã được sử dụng.': 'backendError.voucherInvalid',
  'Voucher catalog không tồn tại.': 'backendError.voucherNotFound',
  'Mã voucher không tồn tại.': 'backendError.voucherNotFound',
  'Bạn chưa có điểm thưởng.': 'backendError.noPoints',
  'Dữ liệu không hợp lệ': 'backendError.invalidData',
  'Bạn không có quyền thực hiện thao tác này.': 'backendError.accessDenied',
  'Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.': 'backendError.systemError',
  'Không tìm thấy người dùng': 'backendError.userNotFound',
  'User không tồn tại.': 'backendError.userNotFound'
}

/**
 * Chuyển lỗi từ API thành message đã localize.
 * Nếu backend message khớp một message VI đã biết → map sang key i18n theo locale hiện tại.
 * Ngược lại → fallbackKey (key i18n).
 */
export function getApiErrorMessage(error: unknown, fallbackKey: string): string {
  const res = (error as any)?.response?.data
  const raw: unknown = res?.message || res?.error
  if (typeof raw === 'string') {
    const key = BACKEND_MESSAGE_KEYS[raw]
    if (key) return i18n.global.t(key)
  }
  return i18n.global.t(fallbackKey)
}
