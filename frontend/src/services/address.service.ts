import http from './http'
import type { Address } from '@/types'

type AddressPayload = Omit<Address, 'id' | 'isDefault'> & { isDefault?: boolean }

export const addressService = {
  async getAll(): Promise<Address[]> {
    return (await http.get('/addresses')).data
  },

  async create(data: AddressPayload): Promise<Address> {
    return (await http.post('/addresses', data)).data
  },

  async update(id: number, data: AddressPayload): Promise<Address> {
    return (await http.put(`/addresses/${id}`, data)).data
  },

  async remove(id: number): Promise<void> {
    await http.delete(`/addresses/${id}`)
  },

  async setDefault(id: number): Promise<void> {
    await http.patch(`/addresses/${id}/default`)
  }
}

export default addressService
