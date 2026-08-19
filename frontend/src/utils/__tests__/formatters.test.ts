import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import i18n from '@/i18n'
import { formatCurrency, formatDate, truncateText, formatRelativeTime } from '@/utils/formatters'

beforeEach(() => {
  i18n.global.locale.value = 'vi'
})

afterEach(() => {
  i18n.global.locale.value = 'vi'
})

describe('formatCurrency', () => {
  it('formats with VND and the Vietnamese đồng sign regardless of locale', () => {
    expect(formatCurrency(1500000).replace(/\s/g, '')).toBe('1.500.000đ')
  })

  it('formats string input', () => {
    expect(formatCurrency('250000').replace(/\s/g, '')).toBe('250.000đ')
  })

  it('still uses VND when locale is en', () => {
    i18n.global.locale.value = 'en'
    expect(formatCurrency(1000)).toContain('đ')
    expect(formatCurrency(1000)).not.toContain('$')
  })

  it('returns 0 đ for invalid input', () => {
    expect(formatCurrency('abc')).toBe('0 đ')
  })
})

describe('formatDate', () => {
  it('formats default date as dd/mm/yyyy', () => {
    expect(formatDate('2026-08-16T00:00:00')).toBe('16/08/2026')
  })

  it('uses vi-VN month names in vi locale', () => {
    const out = formatDate('2026-08-16', { year: 'numeric', month: 'long', day: 'numeric' })
    expect(out).toContain('tháng')
  })

  it('uses en-GB month names in en locale', () => {
    i18n.global.locale.value = 'en'
    const out = formatDate('2026-08-16', { year: 'numeric', month: 'long', day: 'numeric' })
    expect(out).toBe('16 August 2026')
  })

  it('returns empty string for empty or invalid input', () => {
    expect(formatDate('')).toBe('')
    expect(formatDate('not-a-date')).toBe('')
  })

  it('accepts a Date object', () => {
    expect(formatDate(new Date(2026, 0, 5))).toBe('05/01/2026')
  })
})

describe('truncateText', () => {
  it('returns empty string for null/undefined/empty', () => {
    expect(truncateText(null)).toBe('')
    expect(truncateText(undefined)).toBe('')
    expect(truncateText('')).toBe('')
  })

  it('returns text unchanged when at or under limit', () => {
    expect(truncateText('abc', 150)).toBe('abc')
    expect(truncateText('x'.repeat(150))).toBe('x'.repeat(150))
  })

  it('truncates to max chars plus ellipsis when over limit', () => {
    expect(truncateText('x'.repeat(151))).toBe('x'.repeat(150) + '…')
    expect(truncateText('hello world', 5)).toBe('hello…')
  })
})

describe('formatRelativeTime', () => {
  const now = new Date('2026-08-20T12:00:00')

  it('returns "Vừa xong" for less than a minute in vi', () => {
    i18n.global.locale.value = 'vi'
    expect(formatRelativeTime(new Date('2026-08-20T11:59:30'), now)).toBe('Vừa xong')
  })

  it('returns "Just now" in en', () => {
    i18n.global.locale.value = 'en'
    expect(formatRelativeTime(new Date('2026-08-20T11:59:30'), now)).toBe('Just now')
  })

  it('formats minutes/hours/days/months/years in vi', () => {
    i18n.global.locale.value = 'vi'
    expect(formatRelativeTime(new Date('2026-08-20T11:55:00'), now)).toBe('5 phút trước')
    expect(formatRelativeTime(new Date('2026-08-20T10:00:00'), now)).toBe('2 giờ trước')
    expect(formatRelativeTime(new Date('2026-08-18T12:00:00'), now)).toBe('2 ngày trước')
    expect(formatRelativeTime(new Date('2026-07-20T12:00:00'), now)).toBe('1 tháng trước')
    expect(formatRelativeTime(new Date('2025-08-20T12:00:00'), now)).toBe('1 năm trước')
  })

  it('formats minutes/hours/days in en', () => {
    i18n.global.locale.value = 'en'
    expect(formatRelativeTime(new Date('2026-08-20T11:55:00'), now)).toBe('5 minutes ago')
    expect(formatRelativeTime(new Date('2026-08-20T10:00:00'), now)).toBe('2 hours ago')
    expect(formatRelativeTime(new Date('2026-08-18T12:00:00'), now)).toBe('2 days ago')
  })

  it('accepts string input', () => {
    i18n.global.locale.value = 'vi'
    expect(formatRelativeTime('2026-08-20T11:55:00', now)).toBe('5 phút trước')
  })

  it('returns empty string for invalid input', () => {
    expect(formatRelativeTime('not-a-date', now)).toBe('')
  })
})
