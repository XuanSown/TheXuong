import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import i18n from '@/i18n'
import { formatCurrency, formatDate } from '@/utils/formatters'

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
