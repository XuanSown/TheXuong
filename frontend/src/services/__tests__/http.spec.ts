import { describe, it, expect } from 'vitest'
import { shouldRedirectToLogin, LOCKED_REDIRECT_PATH } from '@/services/http'

describe('shouldRedirectToLogin', () => {
  it('redirects on 423 outside login page', () => {
    expect(shouldRedirectToLogin(423, '/checkout')).toBe(true)
  })

  it('does not redirect on 423 when already on /login', () => {
    expect(shouldRedirectToLogin(423, '/login')).toBe(false)
  })

  it('does not redirect on other statuses', () => {
    expect(shouldRedirectToLogin(401, '/checkout')).toBe(false)
    expect(shouldRedirectToLogin(undefined, '/checkout')).toBe(false)
  })
})

describe('LOCKED_REDIRECT_PATH', () => {
  it('points to login with locked flag', () => {
    expect(LOCKED_REDIRECT_PATH).toBe('/login?locked=1')
  })
})
