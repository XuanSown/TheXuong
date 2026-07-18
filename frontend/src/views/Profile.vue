<template>
  <div class="min-h-screen">
    <main class="w-full max-w-[1280px] mx-auto px-4 pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[48px] mb-16">
          <!-- Breadcrumb -->
          <div class="flex items-center gap-3">
            <router-link
              to="/"
              class="flex items-center gap-2 text-[#5E5F5C] hover:text-black transition-colors"
            >
              <svg
                class="w-[13.33px] h-[13.33px]"
                viewBox="0 0 13 13"
                fill="currentColor"
              >
                <path
                  d="M6.5 1L1 6.5l5.5 5.5M1 6.5L6.5 12"
                  stroke="currentColor"
                  stroke-width="1.5"
                  fill="none"
                />
              </svg>
              <span class="font-gelasio text-base">Quay lại trang chủ</span>
            </router-link>
          </div>

          <!-- Page Title -->
          <div class="flex items-center gap-4">
            <svg
              class="w-6 h-6 text-black"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke-width="1.5"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z"
              />
            </svg>
            <h1 class="font-geist text-[20px] leading-[30px] text-black">
              HỒ SƠ CÁ NHÂN
            </h1>
          </div>
        </header>

        <!-- Profile Card -->
        <div
          class="relative w-[713px] max-w-full mx-auto bg-white border border-[#EEEEEE] shadow-[0px_8px_30px_rgba(0,0,0,0.04)] rounded-xl mb-16"
        >
          <!-- Card Header -->
          <div class="flex items-center gap-3 px-8 py-[17px] bg-black">
            <svg
              class="w-5 h-5 text-white"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
            >
              <path
                fill-rule="evenodd"
                d="M7.5 6a4.5 4.5 0 119 0 4.5 4.5 0 01-9 0zM3.751 20.105a8.25 8.25 0 0116.498 0 .75.75 0 01-.437.695A18.683 18.683 0 0112 22.5c-2.786 0-5.433-.608-7.812-1.7a.75.75 0 01-.437-.695z"
                clip-rule="evenodd"
              />
            </svg>
            <h2 class="font-gelasio text-[18px] leading-[28px] tracking-[0.45px] uppercase text-white">
              HỒ SƠ CÁ NHÂN
            </h2>
          </div>

          <!-- Form Content -->
          <div class="p-8 flex flex-col">
            <!-- Profile Fields -->
            <div class="flex flex-col gap-6">
              <!-- Email Field -->
              <div class="flex flex-col gap-2">
                <label
                  class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80"
                >
                  EMAIL
                </label>
                <input
                  type="email"
                  :value="user?.email || ''"
                  disabled
                  class="w-[308px] h-[50px] bg-[#F1F5F9] rounded-lg px-4 font-gelasio text-[16px] text-[#4C4546] outline-none cursor-not-allowed"
                >
              </div>

              <!-- Full Name Field -->
              <div class="flex flex-col gap-2">
                <label
                  class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80"
                >
                  HỌ VÀ TÊN
                </label>
                <input
                  v-model="profileForm.fullName"
                  type="text"
                  placeholder="Nhập họ tên"
                  class="w-[308px] h-[50px] bg-white border border-[#CFC4C5] rounded-lg px-4 font-gelasio text-[16px] text-[#1A1C1C] outline-none focus:border-black transition-colors"
                >
              </div>

              <!-- Phone Number Field -->
              <div class="flex flex-col gap-2">
                <label
                  class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80"
                >
                  SỐ ĐIỆN THOẠI
                </label>
                <input
                  v-model="profileForm.phoneNumber"
                  type="tel"
                  placeholder="Nhập số điện thoại"
                  class="w-[308px] h-[50px] bg-white border border-[#CFC4C5] rounded-lg px-4 font-gelasio text-[16px] text-[#1A1C1C] outline-none focus:border-black transition-colors"
                >
              </div>

              <!-- Sổ địa chỉ -->
              <div class="flex flex-col gap-3">
                <div class="flex items-center justify-between">
                  <label class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80">
                    SỔ ĐỊA CHỈ
                  </label>
                  <button
                    type="button"
                    class="text-sm underline hover:text-black"
                    @click="openCreateAddress"
                  >
                    + Thêm địa chỉ
                  </button>
                </div>
                <div
                  v-if="addressStore.addresses.length === 0"
                  class="text-sm text-gray-500 italic py-2"
                >
                  Chưa có địa chỉ nào. Bấm "Thêm địa chỉ" để tạo mới.
                </div>
                <div
                  v-else
                  class="flex flex-col gap-2"
                >
                  <div
                    v-for="a in addressStore.addresses"
                    :key="a.id"
                    class="flex items-start justify-between p-3 border border-[#EEEEEE] rounded-lg"
                  >
                    <div class="flex-1">
                      <div class="font-medium text-[14px]">
                        {{ a.recipientName }} · {{ a.recipientPhone }}
                        <span
                          v-if="a.isDefault"
                          class="ml-2 text-[10px] bg-black text-white px-1 rounded"
                        >Mặc định</span>
                        <span
                          v-if="a.label"
                          class="ml-2 text-[10px] text-gray-500"
                        >{{ a.label }}</span>
                      </div>
                      <div class="text-sm text-gray-600 mt-1">
                        {{ formatAddress({ streetDetail: a.streetDetail, wardCode: a.wardCode, districtCode: a.districtCode, provinceCode: a.provinceCode }) }}
                      </div>
                    </div>
                    <div class="flex gap-2 text-xs">
                      <button
                        v-if="!a.isDefault"
                        type="button"
                        class="underline hover:text-black"
                        @click="onSetDefault(a.id)"
                      >
                        Đặt mặc định
                      </button>
                      <button
                        type="button"
                        class="underline hover:text-black"
                        @click="openEditAddress(a)"
                      >
                        Sửa
                      </button>
                      <button
                        type="button"
                        class="underline text-red-500 hover:text-red-700"
                        @click="onDeleteAddress(a.id)"
                      >
                        Xóa
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Address Modal -->
              <BaseModal
                v-model="showAddressModal"
                :title="editingAddress ? 'Sửa địa chỉ' : 'Thêm địa chỉ'"
              >
                <AddressForm
                  :model-value="editingAddress || undefined"
                  @submit="onSubmitAddress"
                  @cancel="showAddressModal = false"
                />
              </BaseModal>

              <!-- Separator Line -->
              <div class="w-full h-px bg-[#E8E8E8]" />

              <!-- Change Password Section - Only for LOCAL accounts -->
              <div
                v-if="user?.provider === 'LOCAL'"
                class="flex flex-col gap-6"
              >
                <div class="flex items-center gap-3">
                  <div class="w-[23px] h-[12px] bg-[#4C4546]" />
                  <h3 class="font-geist text-[16px] font-bold leading-[24px] tracking-[0.8px] uppercase text-[#4C4546]">
                    ĐỔI MẬT KHẨU
                  </h3>
                </div>
                <p class="font-gelasio text-[12px] italic text-[#848484]">
                  Bỏ trống nếu không muốn thay đổi mật khẩu.
                </p>

                <div class="flex flex-col gap-4">
                  <!-- Current Password -->
                  <div class="flex flex-col gap-2">
                    <label
                      class="font-geist text-[12px] leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80"
                    >
                      MẬT KHẨU HIỆN TẠI
                    </label>
                    <input
                      v-model="passwordForm.currentPassword"
                      type="password"
                      placeholder="Nhập mật khẩu hiện tại..."
                      class="w-full h-[51px] bg-white border border-[#CFC4C5] rounded-lg px-4 font-gelasio text-[16px] text-[#6B7280] outline-none focus:border-black transition-all"
                    >
                  </div>
                  
                  <div class="flex gap-4">
                    <!-- New Password -->
                    <div class="flex-1 flex flex-col gap-2">
                      <label
                        class="font-geist text-[12px] leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80"
                      >
                        MẬT KHẨU MỚI
                      </label>
                      <input
                        v-model="passwordForm.newPassword"
                        type="password"
                        placeholder="Nhập mật khẩu mới..."
                        class="w-full h-[51px] bg-white border border-[#CFC4C5] rounded-lg px-4 font-gelasio text-[16px] text-[#6B7280] outline-none focus:border-black transition-all"
                      >
                    </div>

                    <!-- Confirm Password -->
                    <div class="flex-1 flex flex-col gap-2">
                      <label
                        class="font-geist text-[12px] leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80"
                      >
                        XÁC NHẬN MẬT KHẨU
                      </label>
                      <input
                        v-model="passwordForm.confirmPassword"
                        type="password"
                        placeholder="Nhập lại mật khẩu mới..."
                        class="w-full h-[51px] bg-white border border-[#CFC4C5] rounded-lg px-4 font-gelasio text-[16px] text-[#6B7280] outline-none focus:border-black transition-all"
                      >
                    </div>
                  </div>
                </div>
              </div>

              <!-- Message for OAuth users -->
              <div
                v-else
                class="flex flex-col gap-4 p-4 bg-[#F3F3F4] rounded-lg"
              >
                <p class="font-gelasio text-sm text-[#5E5F5C]">
                  Tài khoản của bạn được liên kết với Google. Bạn không thể đặt lại mật khẩu thông qua tính năng này.
                </p>
              </div>
            </div>

            <!-- Footer Actions -->
            <div class="mt-12 pt-6 border-t border-[#EEEEEE] flex justify-end gap-4 w-full">
              <button
                class="inline-flex items-center gap-2 px-8 py-3 bg-white border-2 border-black shadow-[0px_4px_4px_rgba(0,0,0,0.25)] rounded-lg hover:bg-gray-50 transition-colors"
                @click="handleCancel"
              >
                <svg
                  class="w-5 h-5"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke-width="2"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M9 15L3 9m0 0l6-6M3 9h12a6 6 0 010 12h-3"
                  />
                </svg>
                <span class="font-geist text-[16px] leading-[24px] text-black">Quay lại</span>
              </button>

              <button
                :disabled="isLoading"
                class="inline-flex items-center gap-2 px-10 py-3 bg-black rounded-lg hover:bg-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                @click="handleSave"
              >
                <svg
                  class="w-5 h-5 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke-width="2"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M4.5 12.75l6 6 9-13.5"
                  />
                </svg>
                <span class="font-geist text-[16px] leading-[24px] text-white">
                  {{ isLoading ? 'Đang lưu...' : 'Lưu thay đổi' }}
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useToast } from 'vue-toastification'
import authService from '@/services/auth.service'
import { useAddressStore } from '@/stores/address.store'
import AddressForm from '@/components/address/AddressForm.vue'
import BaseModal from '@/components/ui/BaseModal.vue'
import { formatAddress } from '@/utils/vn-regions'
import type { Address } from '@/types'

const authStore = useAuthStore()
const toast = useToast()

const user = computed(() => authStore.user)

const profileForm = reactive({
  fullName: '',
  phoneNumber: ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const isLoading = ref(false)

const addressStore = useAddressStore()
const showAddressModal = ref(false)
const editingAddress = ref<Address | null>(null)

const openCreateAddress = () => { editingAddress.value = null; showAddressModal.value = true }
const openEditAddress = (a: Address) => { editingAddress.value = a; showAddressModal.value = true }
const onSubmitAddress = async (data: any) => {
  try {
    if (editingAddress.value) await addressStore.update(editingAddress.value.id, data)
    else await addressStore.create(data)
    showAddressModal.value = false
    toast.success('Lưu địa chỉ thành công')
  } catch (e: any) { toast.error(e.response?.data?.message || 'Không thể lưu địa chỉ') }
}
const onDeleteAddress = async (id: number) => {
  if (!confirm('Xóa địa chỉ này?')) return
  try { await addressStore.remove(id); toast.success('Đã xóa') }
  catch (e: any) { toast.error(e.response?.data?.message || 'Không thể xóa') }
}
const onSetDefault = async (id: number) => {
  try { await addressStore.setDefault(id); toast.success('Đã đặt mặc định') }
  catch (e: any) { toast.error(e.response?.data?.message || 'Không thể đặt mặc định') }
}

onMounted(() => {
  if (user.value) {
    profileForm.fullName = user.value.fullName || ''
    profileForm.phoneNumber = user.value.phoneNumber || ''
  }
  addressStore.fetch()
})

const handleSave = async () => {
  if (passwordForm.newPassword && passwordForm.newPassword !== passwordForm.confirmPassword) {
    toast.error('Mật khẩu xác nhận không khớp!')
    return
  }

  isLoading.value = true
  try {
    await authStore.updateProfile({
      fullName: profileForm.fullName,
      phoneNumber: profileForm.phoneNumber
    })

    if (passwordForm.newPassword) {
      if (!passwordForm.currentPassword) {
        toast.error('Vui lòng nhập mật khẩu hiện tại để đổi mật khẩu mới!')
        isLoading.value = false
        return
      }
      await authService.changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
        confirmPassword: passwordForm.confirmPassword
      })
    }

    toast.success('Cập nhật hồ sơ thành công!')
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('Failed to update profile:', error)
    toast.error('Cập nhật thất bại. Vui lòng thử lại.')
  } finally {
    isLoading.value = false
  }
}

const handleCancel = () => {
  // Reset form to original values
  if (user.value) {
    profileForm.fullName = user.value.fullName || ''
    profileForm.phoneNumber = user.value.phoneNumber || ''
  }
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Gelasio:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap') layer(fonts);

.font-geist {
  font-family: 'Geist', sans-serif;
}

.font-gelasio {
  font-family: 'Geist', sans-serif;
}

.font-inter {
  font-family: 'Geist', sans-serif;
}

/* Custom scrollbar for textarea */
textarea::-webkit-scrollbar {
  width: 6px;
}

textarea::-webkit-scrollbar-track {
  background: transparent;
}

textarea::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

textarea::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>
