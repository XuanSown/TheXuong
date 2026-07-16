import { defineStore } from 'pinia'
import type { Address } from '@/types'
import api from '@/services/api'

export const useAddressStore = defineStore('address', {
  state: () => ({ addresses: [] as Address[], loading: false, loaded: false }),
  getters: {
    defaultAddress: (s) => s.addresses.find(a => a.isDefault) || null,
    hasAddresses: (s) => s.addresses.length > 0
  },
  actions: {
    async fetch() { this.loading = true; try { this.addresses = await api.getAddresses(); this.loaded = true } finally { this.loading = false } },
    async create(data: Parameters<typeof api.createAddress>[0]) { const a = await api.createAddress(data); await this.fetch(); return a },
    async update(id: number, data: Parameters<typeof api.updateAddress>[1]) { const a = await api.updateAddress(id, data); await this.fetch(); return a },
    async remove(id: number) { await api.deleteAddress(id); await this.fetch() },
    async setDefault(id: number) { await api.setDefaultAddress(id); await this.fetch() }
  }
})
