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

// Cắt nội dung review ở mức max ký tự, thêm dấu "…" nếu bị cắt (plan review).
export const truncateText = (text: string | null | undefined, max = 150): string => {
  if (!text) return ''
  if (text.length <= max) return text
  return text.slice(0, max).trimEnd() + '…'
}

// Thời gian tương đối theo locale hiện tại ("3 ngày trước" / "3 days ago").
export const formatRelativeTime = (value: string | Date, now: Date = new Date()): string => {
  const date = typeof value === 'string' ? new Date(value) : value
  if (!value || isNaN(date.getTime())) return ''
  const t = i18n.global.t
  const diffMinutes = Math.floor((now.getTime() - date.getTime()) / 60000)
  if (diffMinutes < 1) return t('review.timeJustNow')
  if (diffMinutes < 60) return t('review.timeMinutesAgo', { n: diffMinutes })
  const hours = Math.floor(diffMinutes / 60)
  if (hours < 24) return t('review.timeHoursAgo', { n: hours })
  const days = Math.floor(hours / 24)
  if (days < 30) return t('review.timeDaysAgo', { n: days })
  const months = Math.floor(days / 30)
  if (months < 12) return t('review.timeMonthsAgo', { n: months })
  return t('review.timeYearsAgo', { n: Math.floor(months / 12) })
}

