export interface Review {
  id: number
  rating: number
  comment: string | null
  createdAt: string
  authorName: string
  verifiedBuyer: boolean
  isMine: boolean
  canModerate: boolean
}

export interface ReviewSummary {
  averageRating: number
  totalCount: number
  distribution: Record<number, number>
}

export interface ReviewListResponse {
  summary: ReviewSummary
  reviews: Review[]
}
