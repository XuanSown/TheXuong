import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import i18n from '@/i18n'
import { getApiErrorMessage } from '@/utils/apiError'

const apiError = (message: string, key = 'message') => ({
  response: { data: { [key]: message } }
})

beforeEach(() => {
  i18n.global.locale.value = 'vi'
})

afterEach(() => {
  i18n.global.locale.value = 'vi'
})

const t = (key: string) => i18n.global.t(key)

describe('getApiErrorMessage', () => {
  it('maps a known backend VI message to its i18n key', () => {
    expect(getApiErrorMessage(apiError('Email hoặc mật khẩu không đúng'), 'errors.generic')).toBe(
      t('backendError.loginFailed')
    )
  })

  it('maps messages returned under the error key', () => {
    expect(getApiErrorMessage(apiError('Email đã được đăng ký', 'error'), 'errors.generic')).toBe(
      t('backendError.emailExists')
    )
  })

  it('maps latin-character backend messages', () => {
    expect(getApiErrorMessage(apiError('Mat khau hien tai khong dung.'), 'errors.generic')).toBe(
      t('backendError.currentPasswordWrong')
    )
  })

  it('translates mapped messages according to the current locale', () => {
    i18n.global.locale.value = 'en'
    expect(getApiErrorMessage(apiError('Email hoặc mật khẩu không đúng'), 'errors.generic')).toBe(
      t('backendError.loginFailed')
    )
  })

  it('returns the fallback for an unknown message', () => {
    expect(getApiErrorMessage(apiError('Điểm không đủ để đổi voucher này'), 'rewards.redeemFailed')).toBe(
      t('rewards.redeemFailed')
    )
  })

  it('returns the fallback for a non-string error body', () => {
    expect(getApiErrorMessage({ response: { data: {} } }, 'errors.generic')).toBe(t('errors.generic'))
    expect(getApiErrorMessage(null, 'errors.generic')).toBe(t('errors.generic'))
  })

  it('returns the fallback for an empty message string', () => {
    expect(getApiErrorMessage(apiError(''), 'errors.generic')).toBe(t('errors.generic'))
  })
})
