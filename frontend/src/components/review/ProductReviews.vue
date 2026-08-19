<template>
  <section
    v-if="productId"
    class="w-[1152px] mx-auto bg-white px-8 py-10 mb-8"
  >
    <h2 class="font-geist text-xl font-semibold text-black mb-8">
      {{ t('review.title') }}
    </h2>

    <!-- Loading -->
    <div
      v-if="loading"
      class="space-y-4"
    >
      <BaseSkeleton
        v-for="i in 3"
        :key="i"
        type="text"
        class="w-full h-16"
      />
    </div>

    <!-- Load error -->
    <p
      v-else-if="error"
      class="text-[#5E5F5C]"
    >
      {{ t('review.loadFailed') }}
    </p>

    <!-- Empty -->
    <p
      v-else-if="!summary || summary.totalCount === 0"
      class="text-[#5E5F5C]"
    >
      {{ t('review.noReviews') }}
    </p>

    <template v-else>
      <!-- Summary header -->
      <div class="flex items-start gap-16 mb-10">
        <div class="flex flex-col items-center w-[180px]">
          <span class="font-geist text-[56px] font-semibold leading-none text-black">
            {{ formatAverage(summary.averageRating) }}
          </span>
          <StarRating
            :model-value="summary.averageRating"
            class="mt-2"
          />
          <span class="text-sm text-[#5E5F5C] mt-2">
            {{ t('review.count', { count: summary.totalCount }) }}
          </span>
        </div>
        <div class="flex-1 max-w-[420px] space-y-1">
          <div
            v-for="stars in [5, 4, 3, 2, 1]"
            :key="stars"
            class="flex items-center gap-3"
          >
            <span class="w-6 text-right text-sm font-medium text-black">{{ stars }}</span>
            <div class="flex-1 h-[6px] bg-[#E5E7EB]">
              <div
                class="h-full bg-black"
                :style="{ width: distributionPercent(stars) }"
              />
            </div>
            <span class="w-8 text-sm text-[#5E5F5C]">{{ summary.distribution[stars] || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- Review list (2 mới nhất, có thể mở rộng) -->
      <div class="border-t border-[#E5E7EB]">
        <article
          v-for="review in visibleReviews"
          :key="review.id"
          class="py-8 border-b border-[#E5E7EB]"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-black text-white flex items-center justify-center font-geist text-sm font-semibold uppercase">
                {{ initials(review.authorName) }}
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <span class="font-geist font-medium text-black">{{ review.authorName }}</span>
                  <span class="inline-flex items-center gap-1 border border-black px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-black">
                    <svg
                      viewBox="0 0 20 20"
                      fill="currentColor"
                      class="w-3 h-3"
                    >
                      <path
                        fill-rule="evenodd"
                        d="M16.403 12.652a3 3 0 000-5.304 3 3 0 00-3.75-3.751 3 3 0 00-5.305 0 3 3 0 00-3.751 3.75 3 3 0 000 5.305 3 3 0 003.75 3.751 3 3 0 005.305 0 3 3 0 003.751-3.75zm-2.546-4.46a.75.75 0 00-1.214-.883l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z"
                        clip-rule="evenodd"
                      />
                    </svg>
                    {{ t('review.verifiedBuyer') }}
                  </span>
                </div>
                <div
                  class="text-xs text-[#5E5F5C] mt-1"
                  :title="formatDate(review.createdAt)"
                >
                  {{ formatRelativeTime(review.createdAt) }}
                </div>
              </div>
            </div>

            <div
              v-if="review.canModerate || review.isMine"
              class="flex items-center gap-3"
            >
              <button
                class="text-xs font-semibold uppercase tracking-wide text-black underline hover:text-[#5E5F5C]"
                @click="openEdit(review)"
              >
                {{ t('review.edit') }}
              </button>
              <button
                v-if="review.canModerate"
                class="text-xs font-semibold uppercase tracking-wide text-black underline hover:text-[#5E5F5C]"
                @click="askDelete(review)"
              >
                {{ t('review.delete') }}
              </button>
            </div>
          </div>

          <StarRating
            :model-value="review.rating"
            size="sm"
            class="mt-3"
          />

          <p
            v-if="review.comment"
            class="mt-3 font-gelasio text-base text-[#4C4546] leading-[26px] whitespace-pre-line"
          >
            {{ expanded.has(review.id) ? review.comment : truncateText(review.comment, 150) }}
            <button
              v-if="review.comment.length > 150"
              class="ml-1 text-sm font-medium text-black underline"
              @click="toggleExpand(review.id)"
            >
              {{ expanded.has(review.id) ? t('review.showLess') : t('review.seeMore') }}
            </button>
          </p>
        </article>
      </div>

      <button
        v-if="summary.totalCount > 2"
        class="mt-6 w-full h-12 border border-black text-black font-geist text-sm font-semibold uppercase tracking-wider hover:bg-black hover:text-white transition-colors"
        @click="showAll = !showAll"
      >
        {{ showAll ? t('review.showLess') : t('review.seeAll', { count: summary.totalCount }) }}
      </button>
    </template>

    <!-- Form viết đánh giá -->
    <div class="border-t border-[#E5E7EB] mt-10 pt-8">
      <p
        v-if="!authStore.isAuthenticated"
        class="text-[#5E5F5C]"
      >
        {{ t('review.needToLogin') }}
        <router-link
          to="/login"
          class="text-black underline font-medium"
        >
          {{ t('common.login') }}
        </router-link>
      </p>

      <template v-else-if="!myReview">
        <h3 class="font-geist text-base font-semibold text-black mb-4">
          {{ t('review.writeTitle') }}
        </h3>
        <p
          v-if="formError"
          class="text-sm mb-3 text-[#4C4546]"
        >
          {{ formError }}
        </p>
        <div class="flex items-center gap-3 mb-4">
          <span class="text-sm text-[#5E5F5C]">{{ t('review.ratingLabel') }}</span>
          <StarRating
            v-model="formRating"
            interactive
          />
        </div>
        <textarea
          v-model="formComment"
          rows="4"
          maxlength="1000"
          class="w-full max-w-[600px] border border-[#7E7576] p-3 font-gelasio text-base text-black focus:border-black focus:outline-none resize-none"
          :placeholder="t('review.commentPlaceholder')"
        />
        <div class="flex items-center gap-4 mt-3">
          <button
            class="h-12 px-8 bg-black text-white font-geist text-sm font-semibold uppercase tracking-wider hover:bg-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="submitting || formRating === 0"
            @click="submitReview"
          >
            {{ submitting ? t('common.loading') : t('review.submit') }}
          </button>
          <span class="text-xs text-[#5E5F5C]">
            {{ t('review.charactersLeft', { count: 1000 - formComment.length }) }}
          </span>
        </div>
      </template>
    </div>

    <!-- Modal sửa đánh giá (dùng chung cho chủ review và admin) -->
    <BaseModal
      v-model="editModalOpen"
      :title="t('review.editTitle')"
    >
      <div class="flex items-center gap-3 mb-4">
        <span class="text-sm text-[#5E5F5C]">{{ t('review.ratingLabel') }}</span>
        <StarRating
          v-model="editRating"
          interactive
        />
      </div>
      <textarea
        v-model="editComment"
        rows="4"
        maxlength="1000"
        class="w-full border border-[#7E7576] p-3 text-base text-black focus:border-black focus:outline-none resize-none"
        :placeholder="t('review.commentPlaceholder')"
      />
      <div class="mt-2 text-xs text-[#5E5F5C]">
        {{ t('review.charactersLeft', { count: 1000 - editComment.length }) }}
      </div>
      <template #footer>
        <button
          class="h-10 px-6 border border-black text-black text-sm font-semibold hover:bg-black hover:text-white transition-colors"
          @click="editModalOpen = false"
        >
          {{ t('review.cancel') }}
        </button>
        <button
          class="h-10 px-6 bg-black text-white text-sm font-semibold hover:bg-gray-900 transition-colors disabled:opacity-50"
          :disabled="editSubmitting || editRating === 0"
          @click="submitEdit"
        >
          {{ editSubmitting ? t('common.loading') : t('review.update') }}
        </button>
      </template>
    </BaseModal>

    <!-- Modal xác nhận xóa -->
    <BaseModal
      v-model="deleteModalOpen"
      :title="t('review.deleteTitle')"
    >
      <p class="text-[#4C4546]">{{ t('review.deleteConfirm') }}</p>
      <template #footer>
        <button
          class="h-10 px-6 border border-black text-black text-sm font-semibold hover:bg-black hover:text-white transition-colors"
          @click="deleteModalOpen = false"
        >
          {{ t('review.cancel') }}
        </button>
        <button
          class="h-10 px-6 bg-black text-white text-sm font-semibold hover:bg-gray-900 transition-colors"
          @click="confirmDelete"
        >
          {{ t('review.delete') }}
        </button>
      </template>
    </BaseModal>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vue-toastification'
import StarRating from '@/components/ui/StarRating.vue'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseSkeleton from '@/components/ui/BaseSkeleton.vue'
import { reviewService } from '@/services/review.service'
import { useAuthStore } from '@/stores/auth.store'
import { formatDate, formatRelativeTime, truncateText } from '@/utils/formatters'
import type { Review, ReviewListResponse } from '@/types/review.types'

const props = defineProps<{ productId: number }>()

const { t, locale } = useI18n()
const toast = useToast()
const authStore = useAuthStore()

const data = ref<ReviewListResponse | null>(null)
const loading = ref(true)
const error = ref(false)
const showAll = ref(false)
const expanded = ref<Set<number>>(new Set())

const formRating = ref(0)
const formComment = ref('')
const formError = ref('')
const submitting = ref(false)

const editModalOpen = ref(false)
const editReviewId = ref<number | null>(null)
const editRating = ref(0)
const editComment = ref('')
const editSubmitting = ref(false)

const deleteModalOpen = ref(false)
const deleteReviewId = ref<number | null>(null)

const summary = computed(() => data.value?.summary ?? null)
const reviews = computed<Review[]>(() => data.value?.reviews ?? [])
const visibleReviews = computed(() =>
  showAll.value ? reviews.value : reviews.value.slice(0, 2)
)
const myReview = computed(() => reviews.value.find((r) => r.isMine) ?? null)

const formatAverage = (avg: number): string => {
  const fixed = avg.toFixed(1)
  return locale.value === 'vi' ? fixed.replace('.', ',') : fixed
}

const distributionPercent = (stars: number): string => {
  const total = summary.value?.totalCount ?? 0
  if (total === 0) return '0%'
  return `${(((summary.value?.distribution[stars] ?? 0) / total) * 100).toFixed(0)}%`
}

const initials = (name: string): string => {
  const parts = name.trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  return (parts.length > 1 ? parts[0][0] + parts[parts.length - 1][0] : parts[0].slice(0, 2)).toUpperCase()
}

const toggleExpand = (id: number) => {
  const next = new Set(expanded.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  expanded.value = next
}

const load = async () => {
  loading.value = true
  error.value = false
  try {
    data.value = await reviewService.getProductReviews(props.productId)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const submitReview = async () => {
  if (formRating.value === 0) return
  submitting.value = true
  formError.value = ''
  try {
    await reviewService.createReview(props.productId, {
      rating: formRating.value,
      comment: formComment.value.trim() || undefined
    })
    toast.success(t('review.submitted'))
    formRating.value = 0
    formComment.value = ''
    await load()
  } catch (e: any) {
    if (e?.response?.status === 403) formError.value = t('review.needToPurchase')
    else if (e?.response?.status === 409) formError.value = t('review.alreadyReviewed')
    else toast.error(t('review.loadFailed'))
  } finally {
    submitting.value = false
  }
}

const openEdit = (review: Review) => {
  editReviewId.value = review.id
  editRating.value = review.rating
  editComment.value = review.comment ?? ''
  editModalOpen.value = true
}

const submitEdit = async () => {
  if (editReviewId.value === null || editRating.value === 0) return
  editSubmitting.value = true
  try {
    await reviewService.updateReview(editReviewId.value, {
      rating: editRating.value,
      comment: editComment.value.trim() || undefined
    })
    toast.success(t('review.updated'))
    editModalOpen.value = false
    await load()
  } catch (e: any) {
    if (e?.response?.status === 403) toast.error(t('errors.accessDenied'))
    else toast.error(t('review.loadFailed'))
  } finally {
    editSubmitting.value = false
  }
}

const askDelete = (review: Review) => {
  deleteReviewId.value = review.id
  deleteModalOpen.value = true
}

const confirmDelete = async () => {
  if (deleteReviewId.value === null) return
  try {
    await reviewService.deleteReview(deleteReviewId.value)
    toast.success(t('review.deleted'))
    deleteModalOpen.value = false
    await load()
  } catch (e: any) {
    if (e?.response?.status === 403) toast.error(t('errors.accessDenied'))
    else toast.error(t('review.loadFailed'))
  }
}

onMounted(load)
</script>
