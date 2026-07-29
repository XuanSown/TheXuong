import { describe, it, expect } from 'vitest'
import { easeOutExpo } from './useCountUp'

describe('easeOutExpo', () => {
  it('starts at 0', () => {
    expect(easeOutExpo(0)).toBe(0)
  })

  it('ends at 1', () => {
    expect(easeOutExpo(1)).toBe(1)
  })

  it('increases monotonically within (0,1)', () => {
    const a = easeOutExpo(0.25)
    const b = easeOutExpo(0.5)
    const c = easeOutExpo(0.75)
    expect(a).toBeGreaterThan(0)
    expect(a).toBeLessThan(b)
    expect(b).toBeLessThan(c)
    expect(c).toBeLessThan(1)
  })
})
