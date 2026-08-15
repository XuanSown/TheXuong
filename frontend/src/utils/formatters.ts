import i18n from '@/i18n'

// Currency luôn là VND bất kể locale (plan Task 7.1: đổi ngôn ngữ không đổi currency).
export const formatCurrency = (value: number | string): string => {
  const num = typeof value === 'string' ? parseFloat(value) : value
  if (isNaN(num)) return '0 đ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(num).replace('₫', 'đ')
}

// Date format theo locale hiện tại (plan Task 7.2 + 7.3): helper chung, không hardcode ở component.
const dateLocale = (): string => (i18n.global.locale.value === 'en' ? 'en-GB' : 'vi-VN')

export const formatDate = (
  value: string | Date,
  options: Intl.DateTimeFormatOptions = { day: '2-digit', month: '2-digit', year: 'numeric' }
): string => {
  if (!value) return ''
  const date = typeof value === 'string' ? new Date(value) : value
  if (isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat(dateLocale(), options).format(date)
}

