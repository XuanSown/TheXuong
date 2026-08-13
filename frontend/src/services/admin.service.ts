import http from './http'
import type { User } from '@/types'

export const adminService = {
	// Users
	async getUsers(): Promise<User[]> {
		const res = (await http.get('/admin/users')).data
		return res.data || []
	},

	async toggleUserActive(id: number): Promise<any> {
		return (await http.patch(`/admin/users/${id}/toggle-active`)).data
	},

	async updateUser(id: number, payload: {
		fullName?: string
		role?: string
		active?: boolean
		password?: string
	}): Promise<any> {
		return (await http.patch(`/admin/users/${id}`, payload)).data
	},

	async deleteUser(id: number): Promise<any> {
		return (await http.delete(`/admin/users/${id}`)).data
	},

	async createUser(data: { email: string; username: string; fullName: string; password: string; role: string }): Promise<any> {
		return (await http.post('/admin/users', data)).data
	},

	// Products
	async getProducts(params?: { page?: number; size?: number; keyword?: string }): Promise<any> {
		return (await http.get('/admin/products', { params })).data
	},

	async getProduct(id: number): Promise<any> {
		return (await http.get(`/admin/products/${id}`)).data
	},

	/**
	 * Create product with multiple image files (1-5 images).
	 * Sends multipart/form-data with fields: name, description, price, imageUrl, sport, brand, category, sizeQuantities, files[]
	 */
	async createProduct(data: {
		name: string
		description: string
		price: number
		imageUrl?: string
		sport?: string
		brand?: string
		category?: string
		sizeQuantities?: Record<string, number>
		files?: File[]
	}): Promise<any> {
		const formData = new FormData()
		formData.append('name', data.name)
		formData.append('description', data.description)
		formData.append('price', String(data.price))
		if (data.imageUrl) formData.append('imageUrl', data.imageUrl)
		if (data.sport) formData.append('sport', data.sport)
		if (data.brand) formData.append('brand', data.brand)
		if (data.category) formData.append('category', data.category)
		if (data.sizeQuantities) {
			formData.append('sizeQuantities', JSON.stringify(data.sizeQuantities))
		}
		if (data.files && data.files.length > 0) {
			data.files.forEach((file) => {
				formData.append('files', file)
			})
		}

		return (await http.post('/admin/products', formData, {
			headers: { 'Content-Type': 'multipart/form-data' }
		})).data
	},

	/**
	 * Update product with multiple image files (1-5 images).
	 * If new files are provided, old images are replaced.
	 */
	async updateProduct(id: number, data: {
		name: string
		description: string
		price: number
		imageUrl?: string
		sport?: string
		brand?: string
		category?: string
		sizeQuantities?: Record<string, number>
		files?: File[]
	}): Promise<any> {
		const formData = new FormData()
		formData.append('name', data.name)
		formData.append('description', data.description)
		formData.append('price', String(data.price))
		if (data.imageUrl) formData.append('imageUrl', data.imageUrl)
		if (data.sport) formData.append('sport', data.sport)
		if (data.brand) formData.append('brand', data.brand)
		if (data.category) formData.append('category', data.category)
		if (data.sizeQuantities) {
			formData.append('sizeQuantities', JSON.stringify(data.sizeQuantities))
		}
		if (data.files && data.files.length > 0) {
			data.files.forEach((file) => {
				formData.append('files', file)
			})
		}

		return (await http.put(`/admin/products/${id}`, formData, {
			headers: { 'Content-Type': 'multipart/form-data' }
		})).data
	},

	async deleteProduct(id: number): Promise<void> {
		await http.delete(`/admin/products/${id}`)
	},

	async toggleProductActive(id: number): Promise<any> {
		return (await http.patch(`/admin/products/${id}/toggle-active`)).data
	},

	// Upload image directly (returns public R2 URL)
	async uploadImage(file: File): Promise<{ url: string }> {
		const formData = new FormData()
		formData.append('file', file)
		return (await http.post('/upload', formData, {
			headers: { 'Content-Type': 'multipart/form-data' }
		})).data
	},

	// Delete image from R2 by URL
	async deleteImage(url: string): Promise<{ message: string }> {
		return (await http.delete('/upload', { data: { url } })).data
	},

	// Orders
	async getOrders(params?: { status?: string; keyword?: string; page?: number; size?: number }): Promise<any> {
		return (await http.get('/admin/orders', { params })).data
	},

	async updateOrderStatus(id: number, status: string): Promise<any> {
		return (await http.patch(`/admin/orders/${id}/status`, { status })).data
	},

	// Statistics
	async getStatistics(): Promise<any> {
		return (await http.get('/admin/statistics')).data
	},

	// Size Catalog
	async getSizeCatalog(typeCode?: string): Promise<any> {
		const params = typeCode ? { typeCode } : undefined
		return (await http.get('/admin/products/size-catalog', { params })).data
	},

	async getSizeTypes(): Promise<any> {
		return (await http.get('/admin/products/size-types')).data
	},

	async createSizeCatalogItem(data: { sizeTypeId: number; name: string; displayOrder?: number }): Promise<any> {
		return (await http.post('/admin/products/size-catalog', data)).data
	},

	async toggleSizeCatalogActive(id: number): Promise<any> {
		return (await http.put(`/admin/products/size-catalog/${id}/toggle-active`)).data
	},

	async deleteSizeCatalogItem(id: number): Promise<any> {
		return (await http.delete(`/admin/products/size-catalog/${id}`)).data
	}
}

export default adminService
