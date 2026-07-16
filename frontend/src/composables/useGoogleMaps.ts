import { importLibrary, setOptions } from '@googlemaps/js-api-loader'

let loaded = false

async function ensureLoaded() {
  if (!loaded) {
    setOptions({
      key: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || '',
      v: 'weekly',
      libraries: ['places'],
      language: 'vi', region: 'VN'
    })
    loaded = true
  }
  return importLibrary('places')
}

// ponytail: Autocomplete gắn vào 1 input, restrict VN, bias theo province đã chọn (optional bounds)
export function useAutocomplete(inputEl: HTMLInputElement, opts: {
  bounds?: google.maps.LatLngBoundsLiteral
  onPlace: (p: { text: string; lat?: number; lng?: number }) => void
}) {
  let ac: google.maps.places.Autocomplete | null = null
  ensureLoaded().then(places => {
    ac = new places.Autocomplete(inputEl, {
      types: ['address'],
      componentRestrictions: { country: 'vn' },
      bounds: opts.bounds,
      strictBounds: false
    })
    ac!.addListener('place_changed', () => {
      const place = ac!.getPlace()
      if (!place.geometry) return
      opts.onPlace({
        text: place.formatted_address || '',
        lat: place.geometry.location?.lat(),
        lng: place.geometry.location?.lng()
      })
    })
  })
  return {
    setBounds: (b?: google.maps.LatLngBoundsLiteral) => { if (ac && b) ac.setBounds(b) },
    destroy: () => { if (ac) { google.maps.event.clearInstanceListeners(ac); ac = null } }
  }
}
