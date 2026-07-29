import { z } from 'zod'

// Common error messages
const messages = {
  required: 'Trường này không được để trống',
  email: 'Email không hợp lệ',
  passwordMin: 'Mật khẩu phải có ít nhất 8 ký tự',
  passwordMatch: 'Mật khẩu không khớp',
  phone: 'Số điện thoại không hợp lệ',
  min0: 'Giá trị phải lớn hơn hoặc bằng 0',
  min1: 'Giá trị phải lớn hơn 0'
}

// Reusable schemas
export const emailSchema = z.string()
  .min(1, messages.required)
  .email(messages.email)

export const passwordSchema = z.string()
  .min(8, messages.passwordMin)

export const phoneSchema = z.string()
  .min(10, messages.phone)
  .regex(/^(0|\+84)[3|5|7|8|9][0-9]{8}$/, messages.phone)

export const requiredStringSchema = z.string()
  .min(1, messages.required)

export const positiveNumberSchema = z.coerce.number()
  .min(0, messages.min0)

// Specific Form Schemas
export const loginSchema = z.object({
  email: emailSchema,
  password: requiredStringSchema
})

export const registerSchema = z.object({
  fullName: requiredStringSchema,
  email: emailSchema,
  password: passwordSchema,
  confirmPassword: requiredStringSchema
}).refine((data) => data.password === data.confirmPassword, {
  message: messages.passwordMatch,
  path: ['confirmPassword']
})

export const forgotPasswordSchema = z.object({
  email: emailSchema
})

export const resetPasswordSchema = z.object({
  password: passwordSchema,
  confirmPassword: requiredStringSchema
}).refine((data) => data.password === data.confirmPassword, {
  message: messages.passwordMatch,
  path: ['confirmPassword']
})

export const productSchema = z.object({
  name: requiredStringSchema,
  description: z.string().optional(),
  category: requiredStringSchema,
  price: positiveNumberSchema.min(1, messages.min1),
  image: z.string().optional(),
  imageUrl: z.string().optional(),
  active: z.boolean().optional(),
  brand: z.string().optional(),
  sport: z.string().optional(),
  sizes: z.record(z.string(), z.number().nullable().optional()).optional()
})

export const checkoutSchema = z.object({
  fullName: requiredStringSchema,
  phoneNumber: phoneSchema,
  address: requiredStringSchema,
  paymentMethod: z.enum(['COD', 'VNPAY']),
  note: z.string().optional()
})
