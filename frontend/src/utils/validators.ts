import { z } from 'zod'
import i18n from '@/i18n'

// Common error messages (lazy: resolved at validation time for current locale)
const errorOf = (key: string) => ({ error: () => i18n.global.t(key) })

// Reusable schemas
export const emailSchema = z.string()
  .min(1, errorOf('validation.required'))
  .email(errorOf('validation.email'))

export const passwordSchema = z.string()
  .min(8, errorOf('validation.passwordMin'))

export const phoneSchema = z.string()
  .min(10, errorOf('validation.phone'))
  .regex(/^(0|\+84)[3|5|7|8|9][0-9]{8}$/, errorOf('validation.phone'))

export const requiredStringSchema = z.string()
  .min(1, errorOf('validation.required'))

export const positiveNumberSchema = z.coerce.number()
  .min(0, errorOf('validation.min0'))

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
  error: () => i18n.global.t('validation.passwordMatch'),
  path: ['confirmPassword']
})

export const forgotPasswordSchema = z.object({
  email: emailSchema
})

export const resetPasswordSchema = z.object({
  password: passwordSchema,
  confirmPassword: requiredStringSchema
}).refine((data) => data.password === data.confirmPassword, {
  error: () => i18n.global.t('validation.passwordMatch'),
  path: ['confirmPassword']
})

export const productSchema = z.object({
  name: requiredStringSchema,
  description: z.string().optional(),
  category: requiredStringSchema,
  price: positiveNumberSchema.min(1, errorOf('validation.min1')),
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
