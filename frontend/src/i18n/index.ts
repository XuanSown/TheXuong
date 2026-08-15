import { createI18n } from 'vue-i18n'
import vi from './locales/vi.json'
import en from './locales/en.json'

export const LOCALE_KEY = 'locale'
export const SUPPORTED_LOCALES = ['vi', 'en'] as const
export type SupportedLocale = (typeof SUPPORTED_LOCALES)[number]

export const detectLocale = (): SupportedLocale => {
  const stored = localStorage.getItem(LOCALE_KEY)
  if (stored === 'vi' || stored === 'en') return stored
  const browser = navigator.language.toLowerCase()
  return browser.startsWith('en') ? 'en' : 'vi'
}

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: detectLocale(),
  fallbackLocale: 'vi',
  messages: { vi, en }
})

export default i18n
