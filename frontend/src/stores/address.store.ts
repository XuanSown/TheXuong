import { defineStore } from 'pinia'
import type { Address } from '@/types'
import addressService from '@/services/address.service'

export const useAddressStore = defineStore('address', {
  state: () => ({ addresses: [] as Address[], loading: false, loaded: false }),
  getters: {
    defaultAddress: (s) => s.addresses.find(a => a.isDefault) || null,
    hasAddresses: (s) => s.addresses.length > 0
  },
  actions: {
    async fetch() { this.loading = true; try { this.addresses = await addressService.getAll(); this.loaded = true } finally { this.loading = false } },
    async create(data: Parameters<typeof addressService.create>[0]) { const a = await addressService.create(data); await this.fetch(); return a },
    async update(id: number, data: Parameters<typeof addressService.update>[1]) { const a = await addressService.update(id, data); await this.fetch(); return a },
    async remove(id: number) { await addressService.remove(id); await this.fetch() },
    async setDefault(id: number) { await addressService.setDefault(id); await this.fetch() }
  }
})
