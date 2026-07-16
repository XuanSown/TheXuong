import data from '@/data/vn-administrative.json'

export interface Region { code: string; name: string; nameWithType: string }
export interface ProvinceNode extends Region { districts: DistrictNode[] }
export interface DistrictNode extends Region { wards: Region[] }

const provinces = data as ProvinceNode[]

export const getProvinces = (): Region[] => provinces.map(({ districts, ...r }) => r)
export const getDistricts = (pc: string): Region[] => provinces.find(p => p.code === pc)?.districts.map(({ wards, ...r }) => r) ?? []
export const getWards = (pc: string, dc: string): Region[] => provinces.find(p => p.code === pc)?.districts.find(d => d.code === dc)?.wards ?? []
export const findProvince = (c: string) => provinces.find(p => p.code === c) ?? null
export const findDistrict = (pc: string, c: string) => findProvince(pc)?.districts.find(d => d.code === c) ?? null
export const findWard = (pc: string, dc: string, c: string) => findDistrict(pc, dc)?.wards.find(w => w.code === c) ?? null

export function formatAddress(a: { streetDetail?: string; wardCode?: string; districtCode?: string; provinceCode?: string }): string {
  const parts: string[] = []
  if (a.streetDetail) parts.push(a.streetDetail.trim())
  const w = a.wardCode && findWard(a.provinceCode!, a.districtCode!, a.wardCode); if (w) parts.push(w.nameWithType)
  const d = a.districtCode && findDistrict(a.provinceCode!, a.districtCode); if (d) parts.push(d.nameWithType)
  const p = a.provinceCode && findProvince(a.provinceCode); if (p) parts.push(p.nameWithType)
  return parts.filter(Boolean).join(', ')
}

// ponytail: match by name instead of centroid — Google address_components is more accurate than Haversine
function norm(s: string): string { return s.toLowerCase().replace(/^(thành phố|tp\.?|quận|huyện|thị xã|phường|xã|thị trấn)\s+/i, '').trim() }

function findProvinceByName(name: string): ProvinceNode | null {
  if (!name) return null
  const n = norm(name)
  return provinces.find(p => norm(p.name) === n || p.name.includes(name) || name.includes(p.name)) ?? null
}

function findDistrictByName(p: ProvinceNode, name: string): DistrictNode | null {
  if (!name) return null
  const n = norm(name)
  return p.districts.find(d => norm(d.name) === n || d.name.includes(name) || name.includes(d.name)) ?? null
}

function findWardByName(d: DistrictNode, name: string): Region | null {
  if (!name) return null
  const n = norm(name)
  return d.wards.find(w => norm(w.name) === n || w.name.includes(name) || name.includes(w.name)) ?? null
}

export interface GoogleAddressComponent { long_name: string; short_name: string; types: string[] }

// ponytail: match Google address_components to VN codes by name — no centroid needed
export function matchByGoogleComponents(components: GoogleAddressComponent[]): { provinceCode: string; districtCode: string; wardCode: string | null } {
  const get = (type: string) => components.find(c => c.types.includes(type))
  const provName = get('administrative_area_level_1')?.long_name || ''
  const distName = get('administrative_area_level_2')?.long_name || ''
  const wardName = get('administrative_area_level_3')?.long_name || get('sublocality')?.long_name || get('sublocality_level_1')?.long_name || get('locality')?.long_name || ''

  const p = findProvinceByName(provName)
  if (!p) return { provinceCode: '', districtCode: '', wardCode: null }
  const d = findDistrictByName(p, distName)
  if (!d) return { provinceCode: p.code, districtCode: '', wardCode: null }
  const w = findWardByName(d, wardName)
  return { provinceCode: p.code, districtCode: d.code, wardCode: w?.code ?? null }
}
