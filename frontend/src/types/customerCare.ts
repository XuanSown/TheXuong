export interface PageData<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface CustomerCareOverview {
  totalFaqs: number
  totalConversations: number
  todayMessages: number
  topIntent: string | null
}

export interface AdminFaq {
  id: number
  topic: string
  questionKeywords: string
  answer: string
  createdAt: string
  updatedAt: string
}

export interface AdminFaqRequest {
  topic: string
  questionKeywords: string
  answer: string
}

export interface AdminChatMemorySummary {
  chatId: string
  updatedAt: string | null
  messageCount: number
  lastMessage: string | null
}

export interface AdminChatMessage {
  role: 'user' | 'assistant'
  content: string
}

export interface AdminConversationDetail {
  chatId: string
  updatedAt: string | null
  messages: AdminChatMessage[]
  parseError: boolean
}

export interface AdminChatLog {
  id: number
  chatId: string
  userName: string | null
  intent: string | null
  userMessage: string
  botReply: string
  createdAt: string
}

export interface LogFilters {
  keyword?: string
  chatId?: string
  intent?: string
  from?: string
  to?: string
}
