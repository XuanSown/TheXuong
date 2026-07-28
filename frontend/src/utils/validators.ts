import { z } from 'zod'
export const loginSchema = z.object({
  email: z.string().email('Email không hợp lệ'),
  password: z.string().min(1, 'Vui lòng nhập mật khẩu')
})
export const registerSchema = z.object({
  fullName: z.string().min(1, 'Vui lòng nhập họ tên'),
  email: z.string().email('Email không hợp lệ'),
  password: z.string().min(8, 'Mật khẩu ít nhất 8 ký tự'),
  confirmPassword: z.string().min(8)
}).refine(d => d.password === d.confirmPassword, { path: ['confirmPassword'], message: 'Mật khẩu không khớp' })
export const forgotPasswordSchema = z.object({ email: z.string().email('Email không hợp lệ') })
export const resetPasswordSchema = z.object({
  password: z.string().min(8, 'Mật khẩu ít nhất 8 ký tự'),
  confirmPassword: z.string().min(8)
}).refine(d => d.password === d.confirmPassword, { path: ['confirmPassword'], message: 'Mật khẩu không khớp' })
export const checkoutSchema = z.object({
  fullName: z.string().min(1, 'Vui lòng nhập họ tên'),
  phoneNumber: z.string().min(1, 'Vui lòng nhập số điện thoại'),
  address: z.string().min(1, 'Vui lòng nhập địa chỉ'),
  paymentMethod: z.enum(['COD', 'VNPAY']),
  note: z.string().optional()
})