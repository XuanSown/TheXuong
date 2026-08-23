import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import Login from '@/views/Login.vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({ query: { locked: '1' } })
}))
vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key }),
  createI18n: () => ({ global: { t: (key: string) => key } })
}))
vi.mock('vee-validate', () => ({
  useForm: () => ({ handleSubmit: (fn: unknown) => fn, isSubmitting: { value: false } }),
  useField: () => ({ value: { value: '' }, errorMessage: { value: '' } })
}))
vi.mock('@vee-validate/zod', () => ({ toTypedSchema: (schema: unknown) => schema }))
vi.mock('vue-toastification', () => ({ useToast: () => ({ error: vi.fn(), success: vi.fn(), info: vi.fn() }) }))
vi.mock('@/stores/auth.store', () => ({
  useAuthStore: () => ({
    login: vi.fn(),
    redirectTo: null,
    setRedirectPath: vi.fn(),
    isAdmin: false
  })
}))
vi.mock('@/utils/apiError', () => ({ getApiErrorMessage: (_e: unknown, key: string) => key }))

const stubs = {
  BaseInput: true,
  BaseButton: true,
  RouterLink: { template: '<a><slot /></a>' }
}

describe('Login.vue locked alert', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })
  afterEach(() => {
    vi.useRealTimers()
  })

  it('shows locked alert when route.query.locked=1', async () => {
    const wrapper = mount(Login, { global: { stubs } })
    await nextTick()
    expect(wrapper.text()).toContain('auth.accountLocked')
  })

  it('hides locked alert after 8 seconds', async () => {
    const wrapper = mount(Login, { global: { stubs } })
    await nextTick()
    vi.advanceTimersByTime(8000)
    await nextTick()
    expect(wrapper.text()).not.toContain('auth.accountLocked')
  })
})
