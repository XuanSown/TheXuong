import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import OrderDetail from '@/views/OrderDetail.vue'

const pushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock }),
  useRoute: () => ({ params: { id: '1' }, query: {} })
}))
vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key }),
  createI18n: () => ({ global: { t: (key: string) => key, locale: { value: 'vi' } } })
}))
vi.mock('@/utils/apiError', () => ({
  getApiErrorMessage: (_e: unknown, key: string) => key
}))

let currentOrder: any = null
const fetchOrderById = vi.fn()
vi.mock('@/stores/order.store', () => ({
  useOrderStore: () => ({
    get currentOrder() {
      return currentOrder
    },
    fetchOrderById
  })
}))

const getProductReviews = vi.fn()
vi.mock('@/services/review.service', () => ({
  reviewService: {
    getProductReviews: (...args: unknown[]) => getProductReviews(...args)
  }
}))

const baseItem = {
  productId: 10,
  productName: 'Giày chạy bộ',
  variantId: 1,
  size: '42',
  quantity: 1,
  price: 100000,
  subtotal: 100000,
  imageUrl: ''
}

const baseOrder = (overrides: Record<string, unknown>) => ({
  id: 1,
  status: 'COMPLETED',
  items: [{ ...baseItem }],
  createdAt: '2026-08-23T10:00:00Z',
  fullName: 'Nguyen Van A',
  phoneNumber: '0900000000',
  address: 'Ha Noi',
  note: '',
  paymentMethod: 'cod',
  subtotal: 100000,
  total: 100000,
  ...overrides
})

const countLabel = (wrapper: ReturnType<typeof mount>, label: string) =>
  wrapper.text().split(label).length - 1

const mountView = async () => {
  const wrapper = mount(OrderDetail, {
    global: {
      stubs: {
        RouterLink: { props: ['to'], template: '<a :href="to"><slot /></a>' }
      }
    }
  })
  await flushPromises()
  return wrapper
}

beforeEach(() => {
  vi.clearAllMocks()
  currentOrder = null
})

describe('OrderDetail review buttons', () => {
  it('renders no review button when order is DELIVERED', async () => {
    currentOrder = baseOrder({ status: 'DELIVERED' })
    const wrapper = await mountView()
    expect(countLabel(wrapper, 'order.reviewProduct')).toBe(0)
    expect(countLabel(wrapper, 'order.reviewed')).toBe(0)
    expect(getProductReviews).not.toHaveBeenCalled()
  })

  it('renders one review button per item when COMPLETED', async () => {
    currentOrder = baseOrder({
      items: [{ ...baseItem }, { ...baseItem, productId: 11 }]
    })
    getProductReviews.mockResolvedValue({ summary: {}, reviews: [] })
    const wrapper = await mountView()
    expect(getProductReviews).toHaveBeenCalledTimes(2)
    expect(getProductReviews).toHaveBeenCalledWith(10)
    expect(getProductReviews).toHaveBeenCalledWith(11)
    expect(countLabel(wrapper, 'order.reviewProduct')).toBe(2)
  })

  it('switches label to Reviewed for products already reviewed by me', async () => {
    currentOrder = baseOrder({
      items: [{ ...baseItem }, { ...baseItem, productId: 11 }]
    })
    getProductReviews.mockImplementation((pid: number) =>
      Promise.resolve(
        pid === 10
          ? { summary: {}, reviews: [{ isMine: true }] }
          : { summary: {}, reviews: [] }
      )
    )
    const wrapper = await mountView()
    expect(countLabel(wrapper, 'order.reviewed')).toBe(1)
    expect(countLabel(wrapper, 'order.reviewProduct')).toBe(1)
  })

  it('navigates to product detail with review=1 on click', async () => {
    currentOrder = baseOrder({})
    getProductReviews.mockResolvedValue({ summary: {}, reviews: [] })
    const wrapper = await mountView()
    const btn = wrapper
      .findAll('button')
      .find((b) => b.text().includes('order.reviewProduct'))
    expect(btn).toBeDefined()
    await btn!.trigger('click')
    expect(pushMock).toHaveBeenCalledWith({
      path: '/product-detail/10',
      query: { review: '1' }
    })
  })

  it('falls back to Review button when the check API fails', async () => {
    currentOrder = baseOrder({})
    getProductReviews.mockRejectedValue(new Error('network'))
    const wrapper = await mountView()
    expect(countLabel(wrapper, 'order.reviewed')).toBe(0)
    expect(countLabel(wrapper, 'order.reviewProduct')).toBe(1)
  })

  it('uses black primary color for breadcrumb link', async () => {
    currentOrder = baseOrder({})
    getProductReviews.mockResolvedValue({ summary: {}, reviews: [] })
    const wrapper = await mountView()
    const backLink = wrapper.find('a[href="/orders"]')
    expect(backLink.classes()).toContain('text-black')
    expect(backLink.classes()).not.toContain('text-[#666666]')
  })
})
