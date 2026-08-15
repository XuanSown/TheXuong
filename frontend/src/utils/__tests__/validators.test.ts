import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import i18n from '@/i18n'
import { loginSchema, registerSchema } from '@/utils/validators'

beforeEach(() => {
  i18n.global.locale.value = 'vi'
})

afterEach(() => {
  i18n.global.locale.value = 'vi'
})

const t = (key: string) => i18n.global.t(key)

describe('validation messages', () => {
  it('rejects a short password with the localized message', () => {
    const result = registerSchema.safeParse({
      fullName: 'Nguyen Van A',
      email: 'user@example.com',
      password: '123',
      confirmPassword: '123'
    })
    expect(result.success).toBe(false)
    if (!result.success) {
      const passwordIssue = result.error.issues.find((i) => i.path[0] === 'password')
      expect(passwordIssue?.message).toBe(t('validation.passwordMin'))
    }
  })

  it('rejects an invalid email with the localized message', () => {
    const result = loginSchema.safeParse({ email: 'not-an-email', password: '12345678' })
    expect(result.success).toBe(false)
    if (!result.success) {
      const emailIssue = result.error.issues.find((i) => i.path[0] === 'email')
      expect(emailIssue?.message).toBe(t('validation.email'))
    }
  })

  it('message follows the current locale (lazy evaluation)', () => {
    const viMessage = t('validation.passwordMin')
    i18n.global.locale.value = 'en'
    const result = registerSchema.safeParse({
      fullName: 'Nguyen Van A',
      email: 'user@example.com',
      password: '123',
      confirmPassword: '123'
    })
    if (!result.success) {
      const passwordIssue = result.error.issues.find((i) => i.path[0] === 'password')
      expect(passwordIssue?.message).toBe(t('validation.passwordMin'))
      expect(passwordIssue?.message).not.toBe(viMessage)
    }
  })

  it('accepts valid credentials', () => {
    const result = loginSchema.safeParse({ email: 'user@example.com', password: '12345678' })
    expect(result.success).toBe(true)
  })
})
