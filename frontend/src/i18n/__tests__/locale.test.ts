import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { defineComponent, h } from 'vue'
import { mount } from '@vue/test-utils'
import i18n, { detectLocale, LOCALE_KEY } from '@/i18n'
import { useLocale } from '@/composables/useLocale'

const Wrapper = defineComponent({
  setup() {
    return useLocale()
  },
  render: () => h('div')
})

const setBrowserLanguage = (value: string) => {
  Object.defineProperty(navigator, 'language', {
    value,
    configurable: true
  })
}

beforeEach(() => {
  i18n.global.locale.value = 'vi'
})

afterEach(() => {
  i18n.global.locale.value = 'vi'
})

describe('detectLocale', () => {
  it('returns vi by default (unsupported browser language)', () => {
    setBrowserLanguage('fr-FR')
    expect(detectLocale()).toBe('vi')
  })

  it('returns en when browser language is English', () => {
    setBrowserLanguage('en-US')
    expect(detectLocale()).toBe('en')
  })

  it('returns vi when browser language is Vietnamese', () => {
    setBrowserLanguage('vi-VN')
    expect(detectLocale()).toBe('vi')
  })

  it('prefers stored locale over browser language', () => {
    setBrowserLanguage('vi-VN')
    localStorage.setItem(LOCALE_KEY, 'en')
    expect(detectLocale()).toBe('en')
  })

  it('ignores an unsupported stored locale', () => {
    setBrowserLanguage('en-US')
    localStorage.setItem(LOCALE_KEY, 'fr')
    expect(detectLocale()).toBe('en')
  })
})

describe('useLocale', () => {
  it('reports current locale', () => {
    const wrapper = mount(Wrapper, { global: { plugins: [i18n] } })
    expect((wrapper.vm as any).currentLocale).toBe('vi')
  })

  it('setLocale switches locale and persists to localStorage', async () => {
    const wrapper = mount(Wrapper, { global: { plugins: [i18n] } })
    ;(wrapper.vm as any).setLocale('en')
    expect((wrapper.vm as any).currentLocale).toBe('en')
    expect(localStorage.getItem(LOCALE_KEY)).toBe('en')
    expect(i18n.global.t('nav.products')).toBe('PRODUCTS')
  })

  it('toggleLocale switches vi -> en and back', () => {
    const wrapper = mount(Wrapper, { global: { plugins: [i18n] } })
    ;(wrapper.vm as any).toggleLocale()
    expect((wrapper.vm as any).currentLocale).toBe('en')
    expect(localStorage.getItem(LOCALE_KEY)).toBe('en')
    ;(wrapper.vm as any).toggleLocale()
    expect((wrapper.vm as any).currentLocale).toBe('vi')
    expect(localStorage.getItem(LOCALE_KEY)).toBe('vi')
  })
})
