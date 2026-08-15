import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { LOCALE_KEY, SUPPORTED_LOCALES, type SupportedLocale } from '@/i18n'

export const useLocale = () => {
  const { locale } = useI18n()

  const currentLocale = computed<SupportedLocale>(() => {
    const value = locale.value
    return SUPPORTED_LOCALES.includes(value as SupportedLocale) ? (value as SupportedLocale) : 'vi'
  })

  const setLocale = (newLocale: SupportedLocale) => {
    locale.value = newLocale
    localStorage.setItem(LOCALE_KEY, newLocale)
  }

  const toggleLocale = () => {
    setLocale(currentLocale.value === 'vi' ? 'en' : 'vi')
  }

  return { currentLocale, setLocale, toggleLocale }
}
