export interface ApiResponse<T = unknown> { data?: T; message?: string; success?: boolean }
export interface PageParams { page?: number; size?: number; sort?: string }