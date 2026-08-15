import { describe, it, expect } from 'vitest'
import vi from '@/i18n/locales/vi.json'
import en from '@/i18n/locales/en.json'

const flattenKeys = (obj: Record<string, unknown>, prefix = ''): string[] =>
  Object.entries(obj).flatMap(([k, v]) =>
    typeof v === 'object' && v !== null
      ? flattenKeys(v as Record<string, unknown>, `${prefix}${k}.`)
      : [`${prefix}${k}`]
  )

const flattenValues = (obj: Record<string, unknown>, prefix = ''): Record<string, string> => {
  const out: Record<string, string> = {}
  for (const [k, v] of Object.entries(obj)) {
    if (typeof v === 'object' && v !== null) {
      Object.assign(out, flattenValues(v as Record<string, unknown>, `${prefix}${k}.`))
    } else {
      out[`${prefix}${k}`] = String(v)
    }
  }
  return out
}

const viKeys = flattenKeys(vi as unknown as Record<string, unknown>)
const enKeys = flattenKeys(en as unknown as Record<string, unknown>)
const viValues = flattenValues(vi as unknown as Record<string, unknown>)
const enValues = flattenValues(en as unknown as Record<string, unknown>)

const placeholders = (value: string): string[] =>
  [...value.matchAll(/\{([^}]+)\}/g)].map((m) => m[1]).sort()

describe('locale parity', () => {
  it('vi and en have the same key set', () => {
    expect(viKeys.length).toBe(enKeys.length)
    expect(viKeys.filter((k) => !enKeys.includes(k))).toEqual([])
    expect(enKeys.filter((k) => !viKeys.includes(k))).toEqual([])
  })

  it('every value is a non-empty string', () => {
    const empty = Object.entries(viValues)
      .filter(([, v]) => v.trim() === '')
      .map(([k]) => k)
    expect(empty).toEqual([])
  })

  it('vi and en use the same interpolation placeholders per key', () => {
    const mismatches: string[] = []
    for (const key of viKeys) {
      const viPlaceholders = placeholders(viValues[key])
      const enPlaceholders = placeholders(enValues[key])
      if (JSON.stringify(viPlaceholders) !== JSON.stringify(enPlaceholders)) {
        mismatches.push(key)
      }
    }
    expect(mismatches).toEqual([])
  })
})
