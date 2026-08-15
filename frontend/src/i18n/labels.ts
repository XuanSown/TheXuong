// Map stored sport values (DB Vietnamese names or English codes) to semantic translation keys.
// Giữ nguyên stored value/code; chỉ map sang key để hiển thị đúng ngôn ngữ.
const SPORT_KEY_MAP: Record<string, string> = {
  'bóng đá': 'football',
  football: 'football',
  'cầu lông': 'badminton',
  badminton: 'badminton',
  'chạy bộ': 'running',
  running: 'running',
  'bóng rổ': 'basketball',
  basketball: 'basketball',
  khác: 'other',
  other: 'other'
}

const FALLBACK_SPORT_KEY = 'other'

export const sportKey = (value: string | null | undefined): string => {
  if (!value) return FALLBACK_SPORT_KEY
  return SPORT_KEY_MAP[value.trim().toLowerCase()] || FALLBACK_SPORT_KEY
}

export const sportTranslationPath = (value: string | null | undefined): string => {
  return `sports.${sportKey(value)}`
}
